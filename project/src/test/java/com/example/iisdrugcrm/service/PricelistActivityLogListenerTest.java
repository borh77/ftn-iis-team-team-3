package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.repository.PricelistActivityLogRepository;
import com.example.iisdrugcrm.service.event.PricelistActionEvent;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PricelistActivityLogListenerTest {

    @Mock
    private PricelistActivityLogRepository repository;

    private PricelistActivityLogListener listener;

    @BeforeEach
    void setUp() {
        listener = new PricelistActivityLogListener(repository);
    }

    @Test
    void handlePersistsActivityLogWithUtcTimestamp() {
        PricelistActionEvent event = new PricelistActionEvent(
                10L,
                99L,
                null,
                PricelistActionType.CREATE,
                "Kreiran cenovnik u statusu DRAFT"
        );

        listener.handle(event);

        ArgumentCaptor<PricelistActivityLog> captor = ArgumentCaptor.forClass(PricelistActivityLog.class);
        verify(repository).save(captor.capture());
        PricelistActivityLog log = captor.getValue();
        assertEquals(10L, log.getPricelistId());
        assertEquals(99L, log.getUserId());
        assertEquals(null, log.getTeamId());
        assertEquals(PricelistActionType.CREATE, log.getActionType());
        assertEquals("Kreiran cenovnik u statusu DRAFT", log.getDescription());
        assertEquals(ZoneOffset.UTC, log.getTimestamp().getOffset());
        assertEquals(null, log.getStatusFrom());
        assertEquals(null, log.getStatusTo());
    }

    @Test
    void handlePersistsStructuredStatusTransitionFields() {
        PricelistActionEvent event = new PricelistActionEvent(
                10L,
                99L,
                null,
                PricelistActionType.STATUS_CHANGE,
                "Promenjen status iz DRAFT u IN_REVIEW",
                PricelistStatus.DRAFT,
                PricelistStatus.IN_REVIEW
        );

        listener.handle(event);

        ArgumentCaptor<PricelistActivityLog> captor = ArgumentCaptor.forClass(PricelistActivityLog.class);
        verify(repository).save(captor.capture());
        PricelistActivityLog log = captor.getValue();
        assertEquals(PricelistActionType.STATUS_CHANGE, log.getActionType());
        assertEquals(PricelistStatus.DRAFT, log.getStatusFrom());
        assertEquals(PricelistStatus.IN_REVIEW, log.getStatusTo());
    }

    @Test
    void handleDoesNotPropagateRepositoryFailure() {
        doThrow(new RuntimeException("Database unavailable")).when(repository).save(any(PricelistActivityLog.class));
        PricelistActionEvent event = new PricelistActionEvent(
                10L,
                99L,
                null,
                PricelistActionType.STATUS_CHANGE,
                "Promenjen status iz DRAFT u IN_REVIEW"
        );

        assertDoesNotThrow(() -> listener.handle(event));
    }
}
