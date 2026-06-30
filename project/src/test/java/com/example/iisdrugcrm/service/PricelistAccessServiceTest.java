package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PricelistAccessServiceTest {

    @Mock
    private PricelistTeamRepository teamRepository;

    @Test
    void ownerCannotActivateAsReviewer() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, team(10L, 99L, 7L));

        assertFalse(service.canActivateAsReviewer(pricelist, 99L));
    }

    @Test
    void assignedTeamMemberCanActivateAsReviewer() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, team(10L, 99L, 7L));

        assertTrue(service.canActivateAsReviewer(pricelist, 7L));
    }

    @Test
    void assignedTeamLeaderCanActivateWhenNotOwner() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, team(10L, 7L, 8L));

        assertTrue(service.canActivateAsReviewer(pricelist, 7L));
    }

    @Test
    void unrelatedUserCannotActivateAsReviewer() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, team(10L, 99L, 7L));

        assertFalse(service.canActivateAsReviewer(pricelist, 8L));
    }

    @Test
    void privatePricelistCannotBeActivatedByNonOwnerTeamColleague() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, null);

        assertFalse(service.canActivateAsReviewer(pricelist, 7L));
    }

    @Test
    void privatePricelistCanBeActivatedByAnotherPricelistCreator() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, null);

        assertTrue(service.canActivateAsReviewer(pricelist, 7L, false, true));
    }

    @Test
    void adminCanActivatePrivatePricelistWhenNotOwner() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, null);

        assertTrue(service.canActivateAsReviewer(pricelist, 7L, true));
    }

    @Test
    void adminCannotActivateOwnPricelist() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, null);

        assertFalse(service.canActivateAsReviewer(pricelist, 99L, true));
    }

    @Test
    void validatingOwnerActivationReturnsClearMessage() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(99L, null);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> service.validateActivationReviewer(pricelist, 99L, true));

        assertEquals(PricelistAccessService.SELF_ACTIVATION_MESSAGE, exception.getMessage());
    }

    @Test
    void createdByNullIsNotOwnedByArbitraryUser() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(null, null);

        assertFalse(service.isOwner(pricelist, 99L));
    }

    @Test
    void unrelatedCreatorCannotCollaborateWithCreatedByNullPricelist() {
        PricelistAccessService service = new PricelistAccessService(teamRepository);
        Pricelist pricelist = pricelist(null, null);

        assertFalse(service.canCollaborate(pricelist, 99L));
    }

    private Pricelist pricelist(Long ownerId, PricelistTeam team) {
        Pricelist pricelist = new Pricelist();
        pricelist.setId(100L);
        pricelist.setCreatedBy(ownerId);
        pricelist.setRegion(new Region("Serbia", "RS"));
        pricelist.setTeam(team);
        return pricelist;
    }

    private PricelistTeam team(Long id, Long leaderId, Long memberId) {
        PricelistTeam team = new PricelistTeam("Review team", leaderId);
        team.setId(id);
        team.setMemberIds(new java.util.LinkedHashSet<>(List.of(memberId)));
        return team;
    }
}
