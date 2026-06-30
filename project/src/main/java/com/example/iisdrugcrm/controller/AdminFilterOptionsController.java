package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.dto.admin.AdminFilterOptionsDTO;
import com.example.iisdrugcrm.dto.admin.AdminLookupOptionDTO;
import com.example.iisdrugcrm.dto.admin.AdminUserLookupOptionDTO;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import java.util.Comparator;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/filter-options")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFilterOptionsController {

    private static final Set<UserRole> PRICELIST_REPORTING_ROLES = Set.of(
            UserRole.ROLE_ADMIN,
            UserRole.ROLE_PRICELIST_CREATOR
    );

    private final PricelistTeamRepository teamRepository;
    private final UserRepository userRepository;

    public AdminFilterOptionsController(PricelistTeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<AdminFilterOptionsDTO> getFilterOptions() {
        var teams = teamRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(this::teamOption)
                .toList();
        var users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "username")).stream()
                .filter(user -> PRICELIST_REPORTING_ROLES.contains(user.getRole()))
                .map(this::userOption)
                .sorted(Comparator.comparing(AdminUserLookupOptionDTO::label, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return ResponseEntity.ok(new AdminFilterOptionsDTO(teams, users));
    }

    private AdminLookupOptionDTO teamOption(PricelistTeam team) {
        String label = hasText(team.getName()) ? team.getName() : "Team #" + team.getId();
        return new AdminLookupOptionDTO(team.getId(), label);
    }

    private AdminUserLookupOptionDTO userOption(User user) {
        String fullName = joinNames(user.getFirstName(), user.getLastName());
        String displayName = hasText(fullName) ? fullName : user.getUsername();
        String detail = hasText(user.getEmail()) ? user.getEmail() : user.getUsername();
        String label = hasText(detail) && !detail.equals(displayName) ? displayName + " - " + detail : displayName;
        return new AdminUserLookupOptionDTO(user.getId(), displayName, user.getEmail(), user.getRole(), label);
    }

    private String joinNames(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        return (first + " " + last).trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
