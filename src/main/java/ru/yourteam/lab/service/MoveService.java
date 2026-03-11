package ru.yourteam.lab.service;

import ru.yourteam.lab.domain.BatchStatus;
import ru.yourteam.lab.domain.ReagentBatch;
import ru.yourteam.lab.domain.StockMove;
import ru.yourteam.lab.domain.StockMoveType;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.BatchRepository;
import ru.yourteam.lab.repository.MoveRepository;
import ru.yourteam.lab.validation.MoveValidator;

import java.time.Instant;

public class MoveService {
    private final MoveRepository moveRepository;
    private final MoveValidator validator;
    private final BatchService batchService;
    private final BatchRepository batchRepository; // Для обновления остатка бутылки

    public MoveService(MoveRepository moveRepository, MoveValidator validator, BatchService batchService, BatchRepository batchRepository) {
        this.moveRepository = moveRepository;
        this.validator = validator;
        this.batchService = batchService;
        this.batchRepository = batchRepository;
    }

    public StockMove makeMove(long batchId, StockMoveType type, double quantity, String reason) {
        // 1. Проверяем бутылку
        ReagentBatch batch = batchService.getById(batchId);

        if (batch.getStatus() == BatchStatus.ARCHIVED) {
            throw new ValidationException("нельзя делать движения для ARCHIVED бутылки");
        }

        // 2. Логика пересчета остатков
        if (type == StockMoveType.OUT || type == StockMoveType.DISCARD) {
            if (batch.getQuantityCurrent() < quantity) {
                throw new ValidationException(String.format("недостаточно остатка (current=%.1f, want=%.1f)",
                        batch.getQuantityCurrent(), quantity));
            }
            batch.setQuantityCurrent(batch.getQuantityCurrent() - quantity);
        } else if (type == StockMoveType.IN) {
            batch.setQuantityCurrent(batch.getQuantityCurrent() + quantity);
        }

        // 3. Создаем движение
        StockMove move = new StockMove();
        move.setBatchId(batchId);
        move.setType(type);
        move.setQuantity(quantity);
        move.setUnit(batch.getUnit()); // Наследуем единицы измерения из бутылки
        move.setReason(reason != null && !reason.isEmpty() ? reason : null);

        validator.validate(move);

        Instant now = Instant.now();
        move.setMovedAt(now);
        move.setCreatedAt(now);

        // 4. Сохраняем и обновляем бутылку
        batch.setUpdatedAt(now);
        batchRepository.save(batch); // Сохраняем обновленный остаток

        return moveRepository.save(move);
    }
    public java.util.List<StockMove> listMoves(long batchId, int limit) {
        // Проверяем существование
        batchService.getById(batchId);

        java.util.List<StockMove> all = moveRepository.findAllByBatchId(batchId);
        // Так как LinkedHashMap сохраняет порядок, новые записи в конце. Нам нужны последние N:
        java.util.Collections.reverse(all);
        if (limit > 0 && all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }
}