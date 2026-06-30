package com.example.iisdrugcrm.dto.admin;

import java.util.List;

public record AdminFilterOptionsDTO(
        List<AdminLookupOptionDTO> teams,
        List<AdminUserLookupOptionDTO> users
) {
}
