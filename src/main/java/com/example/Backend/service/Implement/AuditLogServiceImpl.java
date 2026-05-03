package com.example.Backend.service.Implement;

import com.example.Backend.dto.response.AuditLogResponse;
import com.example.Backend.entity.AuditLog;
import com.example.Backend.entity.User;
import com.example.Backend.repository.AuditLogRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public void saveLog(Long userId, Long targetId, String description) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .targetId(targetId)
                .description(description)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> auditLogPage = auditLogRepository.findAll(pageable);

        Set<Long> userIds = auditLogPage.getContent().stream()
                .map(AuditLog::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return auditLogPage.map(log -> {
            User user = usersById.get(log.getUserId());
            String userName = user != null ? user.getFullName() : null;

            return AuditLogResponse.builder()
                    .id(log.getId())
                    .userId(log.getUserId())
                    .userName(userName)
                    .targetId(log.getTargetId())
                    .description(log.getDescription())
                    .createdAt(log.getCreatedAt())
                    .build();
        });
    }
}
