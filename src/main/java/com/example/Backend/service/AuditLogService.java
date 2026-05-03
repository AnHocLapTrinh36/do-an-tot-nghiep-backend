package com.example.Backend.service;

import com.example.Backend.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;

public interface AuditLogService {
    void saveLog(Long userId, Long targetId, String description);
    Page<AuditLogResponse> getAuditLogs(int page, int size);
}
