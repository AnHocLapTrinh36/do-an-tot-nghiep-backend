package com.example.Backend.service.Implement;

import com.example.Backend.entity.Cv;
import com.example.Backend.entity.JobPost;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectStatus;
import com.example.Backend.entity.Status;
import com.example.Backend.entity.Student;
import com.example.Backend.entity.Technology;
import com.example.Backend.repository.CvRepository;
import com.example.Backend.repository.JobPostRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.repository.StudentRepository;
import com.example.Backend.repository.TechnologyRepository;
import com.example.Backend.service.AiChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {
    private static final int DEFAULT_LIMIT = 10;
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[\\s\\S]*}");

    private final ProjectRepository projectRepository;
    private final CvRepository cvRepository;
    private final StudentRepository studentRepository;
    private final JobPostRepository jobPostRepository;
    private final TechnologyRepository technologyRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    @Override
    @Transactional(readOnly = true)
    public String chat(String message) {
        if (!StringUtils.hasText(message)) {
            return "Bạn vui lòng nhập nội dung cần hỏi nhé.";
        }
        if (!StringUtils.hasText(geminiApiKey)) {
            return "Chưa cấu hình GEMINI_API_KEY nên hiện tại AI chưa thể phản hồi.";
        }

        try {
            AiIntent intent = analyzeIntent(message);
            List<Map<String, Object>> data = queryData(intent);
            return buildFinalReply(message, intent, data);
        } catch (Exception e) {
            log.error("Lỗi xử lý chat AI", e);
            return "Hệ thống AI đang bận một chút, bạn thử lại sau nhé.";
        }
    }

    private AiIntent analyzeIntent(String message) throws IOException, InterruptedException {
        String prompt = """
                Bạn là bộ phân tích câu hỏi cho hệ thống quản lý đồ án sinh viên.
                Chỉ trả về JSON thuần, không Markdown, không giải thích.

                Các intent hợp lệ:
                - SEARCH_STUDENT: tìm sinh viên theo mssv, tên, lớp/khóa/course, kỹ năng hoặc ngôn ngữ trong CV
                - SEARCH_PROJECT: tìm đồ án phù hợp theo tên, mô tả, công nghệ, sinh viên
                - TOP_PROJECT: thống kê đồ án được xem nhiều nhất
                - TOP_CV: thống kê CV được quan tâm nhiều nhất
                - TOP_LANGUAGE: thống kê ngôn ngữ/công nghệ được quan tâm nhiều nhất
                - GENERAL: câu hỏi chung hoặc không thuộc các nhóm trên

                Format bắt buộc:
                {
                  "intent": "SEARCH_STUDENT",
                  "keyword": "React",
                  "course": "D21",
                  "limit": 10
                }

                Quy tắc:
                - keyword là từ khóa chính để tìm kiếm, ví dụ tên, mssv, React, Java, đồ án bán hàng.
                - course chỉ chứa lớp/khóa/course nếu người dùng có nói, ví dụ D21, K21.
                - limit mặc định là 10, tối đa 20.

                Câu hỏi: %s
                """.formatted(message);

        String raw = callGemini(prompt);
        String json = extractJson(raw);
        AiIntent intent = objectMapper.readValue(json, AiIntent.class);
        intent.normalize();
        return intent;
    }

    private List<Map<String, Object>> queryData(AiIntent intent) {
        return switch (intent.getIntent()) {
            case "SEARCH_STUDENT" -> searchStudents(intent);
            case "SEARCH_PROJECT" -> searchProjects(intent);
            case "TOP_PROJECT" -> topProjects();
            case "TOP_CV" -> topCvs(intent);
            case "TOP_LANGUAGE" -> topLanguages(intent);
            default -> List.of();
        };
    }

    private List<Map<String, Object>> searchStudents(AiIntent intent) {
        return studentRepository.searchStudentsForAi(
                        clean(intent.getKeyword()),
                        clean(intent.getCourse()),
                        PageRequest.of(0, intent.safeLimit())
                )
                .stream()
                .map(this::studentToMap)
                .toList();
    }

    private List<Map<String, Object>> searchProjects(AiIntent intent) {
        return projectRepository.searchApprovedProjectsForAi(clean(intent.getKeyword()), PageRequest.of(0, intent.safeLimit()))
                .stream()
                .map(this::projectToMap)
                .toList();
    }

    private List<Map<String, Object>> topProjects() {
        return projectRepository.findTop10ByStatusOrderByViewCountDesc(ProjectStatus.APPROVED)
                .stream()
                .map(this::projectToMap)
                .toList();
    }

    private List<Map<String, Object>> topCvs(AiIntent intent) {
        return cvRepository.findTopPublicApprovedCvsByInterestForAi(PageRequest.of(0, intent.safeLimit()))
                .stream()
                .map(this::cvToMap)
                .toList();
    }

    private List<Map<String, Object>> topLanguages(AiIntent intent) {
        Map<String, Integer> counters = new LinkedHashMap<>();

        for (Project project : projectRepository.findApprovedProjectsWithTechnologiesForAi()) {
            for (Technology technology : project.getTechnologies()) {
                addCounter(counters, technology.getName());
            }
        }

        List<String> knownTechnologies = technologyRepository.findAll()
                .stream()
                .map(Technology::getName)
                .filter(StringUtils::hasText)
                .toList();

        for (Cv cv : cvRepository.findTop50ByStatusAndIsPublicTrueOrderByUpdatedAtDesc(Status.APPROVED)) {
            countKnownTerms(counters, cv.getContentJson(), knownTechnologies);
        }

        for (JobPost jobPost : jobPostRepository.findActiveJobs()) {
            countKnownTerms(counters, jobPost.getTags(), knownTechnologies);
        }

        return counters.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(intent.safeLimit())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("language", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .toList();
    }

    private String buildFinalReply(String userMessage, AiIntent intent, List<Map<String, Object>> data)
            throws JsonProcessingException, IOException, InterruptedException {
        String prompt = """
                Bạn là trợ lý AI của hệ thống quản lý đồ án sinh viên.
                Người dùng hỏi: %s
                Intent nội bộ: %s
                Dữ liệu thật từ hệ thống: %s

                Nhiệm vụ:
                1. Trả lời bằng tiếng Việt tự nhiên, ngắn gọn.
                2. Chỉ dựa trên dữ liệu thật được cung cấp, không tự bịa tên sinh viên, CV, đồ án hoặc số liệu.
                3. Nếu dữ liệu rỗng, hãy nói hiện tại chưa tìm thấy thông tin phù hợp.
                4. Không dùng Markdown, không dùng dấu sao (*), không trả JSON.
                5. Có thể xuống dòng bình thường để dễ đọc.
                """.formatted(userMessage, intent.getIntent(), objectMapper.writeValueAsString(data));

        return callGemini(prompt).trim();
    }

    private String callGemini(String prompt) throws IOException, InterruptedException {
        String escapedKey = URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s"
                .formatted(geminiModel, escapedKey);

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IOException("Gemini API lỗi: " + response.statusCode() + " - " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || !StringUtils.hasText(textNode.asText())) {
            throw new IOException("Gemini API không trả về nội dung hợp lệ");
        }
        return textNode.asText();
    }

    private String extractJson(String raw) {
        Matcher matcher = JSON_OBJECT_PATTERN.matcher(raw);
        if (matcher.find()) {
            return matcher.group();
        }
        return raw;
    }

    private Map<String, Object> studentToMap(Student student) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("mssv", student.getMssv());
        item.put("fullName", student.getUser() != null ? student.getUser().getFullName() : null);
        item.put("email", student.getUser() != null ? student.getUser().getEmail() : null);
        item.put("course", student.getCourse());
        item.put("gpa", student.getGpa());
        item.put("phone", student.getPhone());
        item.put("skillsFromCv", summarizeCvContent(student.getCvs()));
        return item;
    }

    private Map<String, Object> projectToMap(Project project) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", project.getTitle());
        item.put("studentName", project.getStudent() != null && project.getStudent().getUser() != null
                ? project.getStudent().getUser().getFullName()
                : null);
        item.put("description", project.getDescription());
        item.put("viewCount", project.getViewCount());
        item.put("downloadCount", project.getDownloadCount());
        item.put("technologies", project.getTechnologies().stream().map(Technology::getName).toList());
        return item;
    }

    private Map<String, Object> cvToMap(Cv cv) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", cv.getTitle());
        item.put("studentName", cv.getStudent() != null && cv.getStudent().getUser() != null
                ? cv.getStudent().getUser().getFullName()
                : null);
        item.put("mssv", cv.getStudent() != null ? cv.getStudent().getMssv() : null);
        item.put("viewCount", cv.getViewCount());
        item.put("downloadCount", cv.getDownloadCount());
        item.put("interestNote", "Hệ thống hiện dùng lượt xem và lượt tải để biểu thị mức quan tâm của CV.");
        return item;
    }

    private List<String> summarizeCvContent(List<Cv> cvs) {
        if (cvs == null) {
            return List.of();
        }
        return cvs.stream()
                .map(Cv::getContentJson)
                .filter(StringUtils::hasText)
                .limit(3)
                .map(content -> content.length() > 300 ? content.substring(0, 300) : content)
                .toList();
    }

    private void countKnownTerms(Map<String, Integer> counters, String text, List<String> knownTerms) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (String term : knownTerms) {
            if (StringUtils.hasText(term) && lowerText.contains(term.toLowerCase(Locale.ROOT))) {
                addCounter(counters, term);
            }
        }
    }

    private void addCounter(Map<String, Integer> counters, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        counters.merge(value.trim(), 1, Integer::sum);
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    @Data
    private static class AiIntent {
        private String intent = "GENERAL";
        private String keyword;
        private String course;
        private Integer limit = DEFAULT_LIMIT;

        private void normalize() {
            List<String> validIntents = List.of(
                    "SEARCH_STUDENT",
                    "SEARCH_PROJECT",
                    "TOP_PROJECT",
                    "TOP_CV",
                    "TOP_LANGUAGE",
                    "GENERAL"
            );
            if (!validIntents.contains(intent)) {
                intent = "GENERAL";
            }
            if (limit == null || limit < 1) {
                limit = DEFAULT_LIMIT;
            }
            limit = Math.min(limit, 20);
        }

        private int safeLimit() {
            return Objects.requireNonNullElse(limit, DEFAULT_LIMIT);
        }
    }
}
