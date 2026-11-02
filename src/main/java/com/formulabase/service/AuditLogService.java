package com.formulabase.service;

import com.formulabase.domain.AuditLog;
import com.formulabase.domain.User;
import com.formulabase.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;
    public AuditLogService(AuditLogRepository repository) { this.repository = repository; }
    @Transactional public void record(User actor, String action, String subjectType, UUID subjectId, String details) {
        repository.save(new AuditLog(actor, action, subjectType, subjectId, details));
    }
    @Transactional(readOnly = true) public List<AuditLog> search(String keyword) {
        return keyword == null || keyword.isBlank() ? repository.findTop200ByOrderByCreatedAtDesc()
                : repository.searchByKeyword(keyword.trim());
    }
}
