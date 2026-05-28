package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.dto.team.TeamDetailsDTO;
import com.example.iisdrugcrm.dto.team.TeamMemberDTO;
import com.example.iisdrugcrm.exception.DuplicateTeamException;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamServiceImpl implements TeamService {

    private final PricelistTeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamServiceImpl(PricelistTeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TeamDetailsDTO createTeam(String teamName, Long leaderId) {
        String normalizedName = normalize(teamName);
        if (teamRepository.existsByName(normalizedName)) {
            throw new DuplicateTeamException("Team sa tim imenom već postoji");
        }

        requireUser(leaderId);

        PricelistTeam team = new PricelistTeam(normalizedName, leaderId);
        return toDetails(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamDetailsDTO addMemberToTeam(Long teamId, Long memberId, Long currentUserId) {
        PricelistTeam team = requireTeam(teamId);
        ensureLeader(team, currentUserId);

        User member = requireUser(memberId);
        ensurePricelistCreator(member);

        if (memberId.equals(currentUserId)) {
            throw new IllegalArgumentException("Leader ne može da se doda kao član tima");
        }

        team.addMember(memberId);
        return toDetails(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamDetailsDTO removeMemberFromTeam(Long teamId, Long memberId, Long currentUserId) {
        PricelistTeam team = requireTeam(teamId);
        ensureLeader(team, currentUserId);

        team.removeMember(memberId);
        return toDetails(teamRepository.save(team));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDetailsDTO> getTeamsForUser(Long userId) {
        return teamRepository.findTeamsForUser(userId).stream()
                .map(this::toDetails)
                .toList();
    }

    private PricelistTeam requireTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void ensureLeader(PricelistTeam team, Long currentUserId) {
        if (!team.getLeaderId().equals(currentUserId)) {
            throw new IllegalArgumentException("Samo vođa tima može da upravlja članovima");
        }
    }

    private void ensurePricelistCreator(User user) {
        if (user.getRole() != UserRole.ROLE_PRICELIST_CREATOR) {
            throw new IllegalArgumentException("U tim mogu da se dodaju samo korisnici sa ulogom ROLE_PRICELIST_CREATOR");
        }
    }

    private TeamDetailsDTO toDetails(PricelistTeam team) {
        Set<Long> relevantIds = new LinkedHashSet<>(team.getMemberIds());
        relevantIds.add(team.getLeaderId());

        Map<Long, User> usersById = new HashMap<>();
        List<User> users = new ArrayList<>();
        userRepository.findAllById(relevantIds).forEach(users::add);
        for (User user : users) {
            usersById.put(user.getId(), user);
        }

        User leader = usersById.get(team.getLeaderId());
        List<TeamMemberDTO> members = team.getMemberIds().stream()
                .map(usersById::get)
                .filter(user -> user != null)
                .map(TeamMemberDTO::fromEntity)
                .sorted(Comparator.comparing(TeamMemberDTO::getUsername))
                .toList();

        TeamDetailsDTO dto = new TeamDetailsDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setLeaderId(team.getLeaderId());
        dto.setLeaderUsername(leader != null ? leader.getUsername() : null);
        dto.setMemberIds(new LinkedHashSet<>(team.getMemberIds()));
        dto.setMembers(members);
        return dto;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}