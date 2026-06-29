package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PricelistAccessService {

    private final PricelistTeamRepository teamRepository;

    public PricelistAccessService(PricelistTeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public boolean isOwner(Pricelist pricelist, Long userId) {
        return userId != null && pricelist.getCreatedBy() != null && pricelist.getCreatedBy().equals(userId);
    }

    public boolean isSameTeamCreator(Pricelist pricelist, Long userId) {
        if (isOwner(pricelist, userId) || pricelist.getCreatedBy() == null || userId == null) {
            return false;
        }
        return accessibleCreatorIds(userId).contains(pricelist.getCreatedBy());
    }

    public boolean canCollaborate(Pricelist pricelist, Long userId) {
        if (isOwner(pricelist, userId) || isSameTeamCreator(pricelist, userId)) {
            return true;
        }
        if (pricelist.getTeam() == null || userId == null) {
            return false;
        }
        return pricelist.getTeam().getLeaderId().equals(userId) || pricelist.getTeam().getMemberIds().contains(userId);
    }

    public boolean canActivateAsReviewer(Pricelist pricelist, Long userId) {
        return canActivateAsReviewer(pricelist, userId, false);
    }

    public boolean canActivateAsReviewer(Pricelist pricelist, Long userId, boolean admin) {
        if (isOwner(pricelist, userId) || userId == null) {
            return false;
        }
        if (admin) {
            return true;
        }
        if (pricelist.getTeam() == null) {
            return false;
        }
        return pricelist.getTeam().getLeaderId().equals(userId) || pricelist.getTeam().getMemberIds().contains(userId);
    }

    public void validateActivationReviewer(Pricelist pricelist, Long userId) {
        validateActivationReviewer(pricelist, userId, false);
    }

    public void validateActivationReviewer(Pricelist pricelist, Long userId, boolean admin) {
        if (!canActivateAsReviewer(pricelist, userId, admin)) {
            throw new AccessDeniedException("A pricelist must be activated by another authorized reviewer.");
        }
    }

    public void validateOwnerOnly(Pricelist pricelist, Long userId) {
        if (!isOwner(pricelist, userId)) {
            throw new IllegalArgumentException("Only the owner can change this pricelist status.");
        }
    }

    public void validateOwnerOrTeamMember(Pricelist pricelist, Long userId) {
        if (!canCollaborate(pricelist, userId)) {
            throw new IllegalArgumentException("You do not have access to this pricelist.");
        }
    }

    public Set<Long> accessibleCreatorIds(Long userId) {
        Set<Long> userIds = new LinkedHashSet<>();
        userIds.add(userId);
        for (PricelistTeam team : teamRepository.findTeamsForUser(userId)) {
            userIds.add(team.getLeaderId());
            userIds.addAll(team.getMemberIds());
        }
        return userIds;
    }
}
