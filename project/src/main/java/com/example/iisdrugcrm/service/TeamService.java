package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.team.TeamDetailsDTO;
import java.util.List;

public interface TeamService {

    TeamDetailsDTO createTeam(String teamName, Long leaderId);

    TeamDetailsDTO addMemberToTeam(Long teamId, Long memberId, Long currentUserId);

    TeamDetailsDTO removeMemberFromTeam(Long teamId, Long memberId, Long currentUserId);

    List<TeamDetailsDTO> getTeamsForUser(Long userId);
}