package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.repository.PricelistActivityLogRepository;
import com.example.iisdrugcrm.service.event.PricelistActionEvent;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PricelistActivityLogListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PricelistActivityLogListener.class);

    private final PricelistActivityLogRepository repository;

    public PricelistActivityLogListener(PricelistActivityLogRepository repository) {
        this.repository = repository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PricelistActionEvent event) {
        try {
            repository.save(new PricelistActivityLog(
                    event.pricelistId(),
                    event.userId(),
                    event.teamId(),
                    event.actionType(),
                    event.description(),
                    OffsetDateTime.now(ZoneOffset.UTC)
            ));
        } catch (Exception exception) {
            LOGGER.warn("Failed to persist pricelist activity log for pricelist {}: {}", event.pricelistId(), exception.getMessage());
            LOGGER.debug("Pricelist activity log persistence failure", exception);
        }
    }
}
