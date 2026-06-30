package com.example.iisdrugcrm.dto.admin;

import com.example.iisdrugcrm.domain.UserRole;

public record AdminUserLookupOptionDTO(
        Long id,
        String displayName,
        String email,
        UserRole role,
        String label
) {
}
