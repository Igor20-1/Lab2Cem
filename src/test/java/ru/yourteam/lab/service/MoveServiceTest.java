// PATH: .\src\test\java\ru\yourteam\lab\service\MoveServiceTest.java
package ru.yourteam.lab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yourteam.lab.domain.*;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.BatchRepository;
import ru.yourteam.lab.repository.MoveRepository;
import ru.yourteam.lab.repository.ReagentRepository;
import ru.yourteam.lab.repository.inmemory.InMemoryBatchRepository;
import ru.yourteam.lab.repository.inmemory.InMemoryMoveRepository;
import ru.yourteam.lab.repository.inmemory.InMemoryReagentRepository;
import ru.yourteam.lab.validation.BatchValidator;
import ru.yourteam.lab.validation.MoveValidator;
import ru.yourteam.lab.validation.ReagentValidator;

import static org.junit.jupiter.api.Assertions.*;

class MoveServiceTest {
    private MoveService moveService;
    private BatchService batchService;
    private long batchId;

    @BeforeEach
    void setUp() {
        ReagentRepository reagentRepo = new InMemoryReagentRepository();
        BatchRepository batchRepo = new InMemoryBatchRepository();
        MoveRepository moveRepo = new InMemoryMoveRepository();

        ReagentService reagentService = new ReagentService(reagentRepo, new ReagentValidator());
        batchService = new BatchService(batchRepo, new BatchValidator(), reagentService);
        moveService = new MoveService(moveRepo, new MoveValidator(), batchService, batchRepo);

        Reagent r = reagentService.createReagent("Water", "H2O", null, null);
        ReagentBatch batch = batchService.createBatch(r.getId(), "W-01", 1000.0, BatchUnit.ML, "Fridge", null);
        batchId = batch.getId();
    }

    @Test
    void makeMove_Out_Success_DecreasesQuantity() {
        // Act
        StockMove move = moveService.makeMove(batchId, StockMoveType.OUT, 200.0, "For experiment");

        // Assert
        assertNotNull(move);
        assertEquals(StockMoveType.OUT, move.getType());
        assertEquals(800.0, batchService.getById(batchId).getQuantityCurrent());
    }

    @Test
    void makeMove_In_Success_IncreasesQuantity() {
        moveService.makeMove(batchId, StockMoveType.IN, 500.0, "Returned unused");
        assertEquals(1500.0, batchService.getById(batchId).getQuantityCurrent());
    }

    @Test
    void makeMove_Out_NotEnoughQuantity_ThrowsException() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
                moveService.makeMove(batchId, StockMoveType.OUT, 1200.0, "Too much")
        );
        assertTrue(exception.getMessage().contains("недостаточно остатка"));
        // Остаток не должен измениться
        assertEquals(1000.0, batchService.getById(batchId).getQuantityCurrent());
    }

    @Test
    void makeMove_ArchivedBatch_ThrowsException() {
        batchService.archiveBatch(batchId);

        assertThrows(ValidationException.class, () ->
                moveService.makeMove(batchId, StockMoveType.OUT, 100.0, "Try to move")
        );
    }
}