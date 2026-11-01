package com.formulabase.repository;

import com.formulabase.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
    List<AuditLog> findByActionContainingIgnoreCaseOrDetailsContainingIgnoreCaseOrderByCreatedAtDesc(String action, String details);
}
