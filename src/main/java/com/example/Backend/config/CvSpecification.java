package com.example.Backend.config;
import com.example.Backend.entity.CVType;
import org.springframework.data.jpa.domain.Specification;
import com.example.Backend.entity.Cv;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CvSpecification {

    public static Specification<Cv> filterPublicApprovedCvs(String keyword, String type, List<String> techs) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Điều kiện bắt buộc: is_approved = true và is_public = true
            predicates.add(cb.equal(root.get("isApproved"), true));
            predicates.add(cb.equal(root.get("isPublic"), true));

            // Lọc theo Title (Keyword)
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }

            // Lọc theo Type (FORM / UPLOAD)
            if (type != null && !type.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("type"), CVType.valueOf(type)));
            }

            // Lọc theo danh sách công nghệ (Checkbox) tìm trong content_json
            if (techs != null && !techs.isEmpty()) {
                List<Predicate> techPredicates = new ArrayList<>();
                for (String tech : techs) {
                    // Dùng dấu ngoặc kép để tìm chính xác phần tử trong mảng JSON.
                    String searchPattern = "%\"" + tech + "\"%";

                    // Nếu CV là UPLOAD, contentJson có thể null, nên ta chỉ tìm khi type là FORM hoặc contentJson không null
                    techPredicates.add(cb.like(root.get("contentJson"), searchPattern));
                }
                // Nối các điều kiện skill bằng OR (Chỉ cần có 1 trong các skill là thoả mãn)
                predicates.add(cb.or(techPredicates.toArray(new Predicate[0])));
            }

            // Nối tất cả các cụm điều kiện lại bằng AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}