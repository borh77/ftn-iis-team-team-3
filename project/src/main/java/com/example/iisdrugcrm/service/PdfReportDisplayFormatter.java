package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PdfReportDisplayFormatter {

    private static final ZoneId REPORT_ZONE = ZoneId.of("Europe/Belgrade");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final UserRepository userRepository;
    private final PricelistTeamRepository teamRepository;

    public PdfReportDisplayFormatter(UserRepository userRepository, PricelistTeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    public Map<Long, User> usersById(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userRepository.findAllById(userIds.stream().filter(Objects::nonNull).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    public Map<Long, PricelistTeam> teamsById(Collection<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return teamRepository.findAllById(teamIds.stream().filter(Objects::nonNull).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(PricelistTeam::getId, Function.identity()));
    }

    public String formatUser(Long userId, Map<Long, User> usersById) {
        if (userId == null) {
            return "System";
        }
        User user = usersById == null ? null : usersById.get(userId);
        if (user == null) {
            return "User #" + userId;
        }
        return formatUser(user);
    }

    public String formatUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return "-";
        }
        return userRepository.findByUsername(username)
                .map(this::formatUser)
                .orElse(username);
    }

    public String formatTeam(Long teamId, Map<Long, PricelistTeam> teamsById) {
        if (teamId == null) {
            return "Private / No team";
        }
        PricelistTeam team = teamsById == null ? null : teamsById.get(teamId);
        if (team == null) {
            return "Team #" + teamId;
        }
        return blankToNull(team.getName()) == null ? "Team #" + teamId : team.getName();
    }

    public String formatUserFilter(Long userId, Map<Long, User> usersById) {
        return userId == null ? "All users" : formatUser(userId, usersById);
    }

    public String formatTeamFilter(Long teamId, Map<Long, PricelistTeam> teamsById) {
        return teamId == null ? "All teams" : formatTeam(teamId, teamsById);
    }

    public String formatDateTime(OffsetDateTime timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(timestamp.atZoneSameInstant(REPORT_ZONE));
    }

    public String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatUser(User user) {
        String fullName = fullName(user);
        String email = blankToNull(user.getEmail());
        String username = blankToNull(user.getUsername());
        if (fullName != null && email != null) {
            return fullName + " <" + email + ">";
        }
        if (email != null) {
            return email;
        }
        if (username != null) {
            return username;
        }
        return "User #" + user.getId();
    }

    private String fullName(User user) {
        String firstName = blankToNull(user.getFirstName());
        String lastName = blankToNull(user.getLastName());
        if (firstName == null && lastName == null) {
            return null;
        }
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
