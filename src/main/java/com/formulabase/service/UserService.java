package com.formulabase.service;

import com.formulabase.domain.User;
import com.formulabase.domain.UserRole;
import com.formulabase.repository.UserRepository;
import com.formulabase.repository.ProductionPlanRepository;
import com.formulabase.domain.ProductionPlan;
import com.formulabase.domain.ProductionPlanStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService audit;
    private final ProductionPlanRepository plans;
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, AuditLogService audit, ProductionPlanRepository plans) { this.repository = repository; this.passwordEncoder = passwordEncoder; this.audit = audit; this.plans = plans; }

    @Transactional
    public void ensurePrimaryAdministrator() {
        var admins = repository.lockAdministrators();
        if (admins.stream().anyMatch(User::isPrimaryAdmin)) return;
        admins.stream().filter(u -> u.isActive() && !u.isArchived())
            .sorted(java.util.Comparator.comparing(u -> !u.getUsername().equalsIgnoreCase("ceo")))
            .findFirst().ifPresent(User::protectAsPrimaryAdmin);
    }

    // Serialize administrator mutations in one deterministic lock order. Recheck
    // the persisted actor after taking the locks, not a detached session snapshot.
    private User requireActiveAdministrator(User actor) {
        if (actor == null || actor.getId() == null) throw new IllegalStateException("Only CEO can manage users");
        return repository.lockAdministrators().stream()
                .filter(current -> current.getId().equals(actor.getId()) && current.isActive() && !current.isArchived())
                .findFirst().orElseThrow(() -> new IllegalStateException("Only CEO can manage users"));
    }

    @Transactional
    public User authenticate(String username, String password) {
        User user = repository.findByUsernameIgnoreCase(username)
                .filter(candidate -> candidate.isActive() && !candidate.isArchived())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        boolean legacyPassword = user.getPassword() != null && !user.getPassword().startsWith("$2");
        boolean matches = password != null && (legacyPassword ? java.security.MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8), user.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8)) : passwordEncoder.matches(password, user.getPassword()));
        if (!matches) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (legacyPassword || passwordEncoder.upgradeEncoding(user.getPassword())) user.setPassword(passwordEncoder.encode(password));
        return user;
    }

    @Transactional(readOnly = true)
    public User findActive(String username) {
        return repository.findByUsernameIgnoreCase(username)
                .filter(user -> user.isActive() && !user.isArchived())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<User> workers() { return repository.findByRoleAndActiveTrueAndArchivedFalse(UserRole.WORKER); }
    @Transactional(readOnly = true)
    public List<User> all() { return repository.findByArchivedFalse(); }
    @Transactional(readOnly = true)
    public User visibleUser(UUID id) {
        return repository.findById(id).filter(user -> !user.isArchived()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    @Transactional
    public User create(String username, String password, String fullName, UserRole role, User actor) {
        actor = requireActiveAdministrator(actor);
        if (username == null || username.isBlank() || password == null || password.isBlank() || fullName == null || fullName.isBlank() || role == null) throw new IllegalArgumentException("Complete all user fields");
        var existing = repository.findByUsernameIgnoreCase(username.trim());
        if (existing.isPresent()) throw new IllegalArgumentException(existing.get().isArchived() ? "Username belongs to a removed account" : "Username already exists");
        if (password.length() < 10) throw new IllegalArgumentException("Password must contain at least 10 characters");
        User created = repository.save(new User(username.trim(), passwordEncoder.encode(password), fullName.trim(), role));
        audit.record(actor,"CREATE_USER","USER",created.getId(),"Created @"+created.getUsername());
        return created;
    }
    @Transactional
    public User toggle(java.util.UUID id, User actor) {
        actor = requireActiveAdministrator(actor);
        User user = repository.findLockedById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.isArchived()) throw new IllegalArgumentException("User not found");
        if (user.isPrimaryAdmin()) throw new IllegalArgumentException("Primary administrator is protected");
        if (user.getId().equals(actor.getId())) throw new IllegalArgumentException("CEO cannot deactivate the current account");
        if (user.isActive() && !unfinishedTasks(id).isEmpty()) throw new IllegalStateException("Transfer unfinished work before deactivating this account");
        user.setActive(!user.isActive());
        audit.record(actor,"TOGGLE_USER","USER",id,"User @"+user.getUsername()+" active="+user.isActive());
        return user;
    }

    @Transactional
    public void delete(java.util.UUID id, User actor) {
        deleteWithTransfers(id, actor, Map.of(), Map.of());
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> unfinishedTasks(UUID id) {
        return plans.findByAssignedWorkerIdAndStatusInOrderByPlannedDateAsc(id, List.of(ProductionPlanStatus.PLANNED, ProductionPlanStatus.IN_PROGRESS));
    }

    @Transactional
    public void deleteWithTransfers(UUID id, User actor, Map<UUID, UUID> replacements, Map<UUID, Long> versions) {
        finishAccountTransfer(id, actor, replacements, versions, true);
    }

    @Transactional
    public void deactivateWithTransfers(UUID id, User actor, Map<UUID, UUID> replacements, Map<UUID, Long> versions) {
        finishAccountTransfer(id, actor, replacements, versions, false);
    }

    private void finishAccountTransfer(UUID id, User actor, Map<UUID, UUID> replacements, Map<UUID, Long> versions, boolean archive) {
        actor = requireActiveAdministrator(actor);
        User user = repository.findLockedById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (id.equals(actor.getId())) throw new IllegalArgumentException("Cannot delete your own account");
        if (user.isArchived()) throw new IllegalArgumentException("User not found");
        if (user.isPrimaryAdmin()) throw new IllegalArgumentException("Primary administrator is protected");
        if (!archive && !user.isActive()) throw new IllegalStateException("Account is already inactive");
        List<ProductionPlan> open = unfinishedTasks(id);
        var required = open.stream().map(ProductionPlan::getId).collect(java.util.stream.Collectors.toSet());
        if (!required.equals(replacements.keySet()) || !required.equals(versions.keySet())) throw new IllegalArgumentException("Choose a replacement for every unfinished task");
        var validated = new java.util.HashMap<UUID, User>();
        for (var plan : open) {
            if (plan.getVersion() != versions.get(plan.getId())) throw new IllegalStateException("Tasks changed; review the list and try again");
            User next = repository.findLockedById(replacements.get(plan.getId())).orElseThrow(() -> new IllegalArgumentException("Select an active replacement worker"));
            if (next.getId().equals(id) || next.getRole() != UserRole.WORKER || !next.isActive() || next.isArchived()) throw new IllegalArgumentException("Select an active replacement worker");
            validated.put(plan.getId(), next);
        }
        for (var plan : open) {
            User next = validated.get(plan.getId());
            plan.assignWorker(next);
            audit.record(actor, "TRANSFER_WORK", "PLAN", plan.getId(), user.getFullName() + " (@" + user.getUsername() + ") → " + next.getFullName() + " (@" + next.getUsername() + ") · " + plan.getFormula().getDocumentNo() + " · " + plan.getPlannedDate());
        }
        if (archive) {
            user.archive();
            audit.record(actor, "DELETE_USER", "USER", id, "Removed @" + user.getUsername() + " from user management; history retained");
        } else {
            user.setActive(false);
            audit.record(actor, "TOGGLE_USER", "USER", id, "User @" + user.getUsername() + " active=false");
        }
    }
}
