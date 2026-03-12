// PATH: .\src\test\java\ru\yourteam\lab\service\BatchServiceTest.java
package ru.yourteam.lab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yourteam.lab.domain.BatchStatus;
import ru.yourteam.lab.domain.BatchUnit;
import ru.yourteam.lab.domain.Reagent;
import ru.yourteam.lab.domain.ReagentBatch;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.BatchRepository;
import ru.yourteam.lab.repository.ReagentRepository;
import ru.yourteam.lab.repository.inmemory.InMemoryBatchRepository;
import ru.yourteam.lab.repository.inmemory.InMemoryReagentRepository;
import ru.yourteam.lab.validation.BatchValidator;
import ru.yourteam.lab.validation.ReagentValidator;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BatchServiceTest {
    private BatchService batchService;
    private ReagentService reagentService;

    @BeforeEach
    void setUp() {
        ReagentRepository reagentRepo = new InMemoryReagentRepository();
        BatchRepository batchRepo = new InMemoryBatchRepository();

        reagentService = new ReagentService(reagentRepo, new ReagentValidator());
        batchService = new BatchService(batchRepo, new BatchValidator(), reagentService);
    }

    @Test
    void createBatch_Success() {
        // Arrange
        Reagent r = reagentService.createReagent("NaCl", "NaCl", null, null);

        // Act
        ReagentBatch batch = batchService.createBatch(r.getId(), "L-123", 500.0, BatchUnit.G, "Shelf-1", Instant.now());

        // Assert
        assertNotNull(batch);
        assertEquals(1, batch.getId());
        assertEquals(500.0, batch.getQuantityCurrent());
        assertEquals(BatchStatus.ACTIVE, batch.getStatus());
    }

    @Test
    void createBatch_ReagentNotFound_ThrowsException() {
        // Assert
        assertThrows(ValidationException.class, () ->
                batchService.createBatch(999L, "L-123", 500.0, BatchUnit.G, "Shelf-1", Instant.now())
        );
    }

    @Test
    void archiveBatch_Success() {
        Reagent r = reagentService.createReagent("NaCl", null, null, null);
        ReagentBatch batch = batchService.createBatch(r.getId(), "L-123", 500.0, BatchUnit.G, "Shelf-1", null);

        batchService.archiveBatch(batch.getId());

        assertEquals(BatchStatus.ARCHIVED, batchService.getById(batch.getId()).getStatus());
    }
}