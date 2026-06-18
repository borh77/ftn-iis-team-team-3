package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class PricelistAccessServiceTest {

    @Mock
    private PricelistTeamRepository teamRepository;

    private PricelistAccessService service;

    @BeforeEach
    void setUp() {
        service = new PricelistAccessService(teamRepository);
    }

    @Test
    void createdByNullIsNotOwnedByArbitraryUser() {
        Pricelist pricelist = new Pricelist();
        pricelist.setCreatedBy(null);

        assertFalse(service.isOwner(pricelist, 99L));
    }

    @Test
    void unrelatedCreatorCannotCollaborateWithCreatedByNullPricelist() {
        Pricelist pricelist = new Pricelist();
        pricelist.setCreatedBy(null);

        assertFalse(service.canCollaborate(pricelist, 99L));
    }
}
