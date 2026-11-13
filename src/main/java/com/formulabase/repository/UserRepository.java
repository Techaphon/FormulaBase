package com.formulabase.repository;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.role = com.formulabase.domain.UserRole.CEO order by u.username")
    List<User> lockAdministrators();
    Optional<User> findByUsernameIgnoreCase(String username);
    List<User> findByRoleAndActiveTrueAndArchivedFalse(UserRole role);
    List<User> findByArchivedFalse();
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id")
    Optional<User> findLockedById(@org.springframework.data.repository.query.Param("id") UUID id);
}
