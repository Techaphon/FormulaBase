package com.formulabase.repository;

import com.formulabase.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
    @Query("""
            select a from AuditLog a left join a.actor actor
            where lower(a.action) like lower(concat('%', :keyword, '%'))
               or lower(a.details) like lower(concat('%', :keyword, '%'))
               or lower(actor.username) like lower(concat('%', :keyword, '%'))
               or lower(actor.fullName) like lower(concat('%', :keyword, '%'))
            order by a.createdAt desc
            """)
    List<AuditLog> searchByKeyword(@Param("keyword") String keyword);
}
