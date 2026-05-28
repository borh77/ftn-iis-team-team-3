package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.team.CreateTeamDTO;
import com.example.iisdrugcrm.dto.team.TeamDetailsDTO;
import com.example.iisdrugcrm.dto.team.TeamMemberRequestDTO;
import com.example.iisdrugcrm.service.TeamService;
import com.example.iisdrugcrm.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@PreAuthorize("hasRole('PRICELIST_CREATOR')")
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    public TeamController(TeamService teamService, UserService userService) {
        this.teamService = teamService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TeamDetailsDTO> create(Authentication authentication, @Valid @RequestBody CreateTeamDTO dto) {
        Long leaderId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(dto.getTeamName(), leaderId));
    }

    @PutMapping("/{id}/members/add")
    public ResponseEntity<TeamDetailsDTO> addMember(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TeamMemberRequestDTO dto
    ) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(teamService.addMemberToTeam(id, dto.getMemberId(), currentUserId));
    }

    @PutMapping("/{id}/members/remove")
    public ResponseEntity<TeamDetailsDTO> removeMember(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TeamMemberRequestDTO dto
    ) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(teamService.removeMemberFromTeam(id, dto.getMemberId(), currentUserId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TeamDetailsDTO>> getMyTeams(Authentication authentication) {
        Long currentUserId = userService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(teamService.getTeamsForUser(currentUserId));
    }
}