package ru.yourteam.lab.service;

import ru.yourteam.lab.domain.BatchStatus;
import ru.yourteam.lab.domain.BatchUnit;
import ru.yourteam.lab.domain.ReagentBatch;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.BatchRepository;
import ru.yourteam.lab.validation.BatchValidator;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class BatchService {
    private final BatchRepository batchRepository;
    private final BatchValidator validator;
    private final ReagentService reagentService;

    public BatchService(BatchRepository batchRepository, BatchValidator validator, ReagentService reagentService) {
        this.batchRepository = batchRepository;
        this.validator = validator;
        this.reagentService = reagentService;
    }

    public ReagentBatch createBatch(long reagentId, String label, double quantity, BatchUnit unit, String location, Instant expiresAt) {
        reagentService.getById(reagentId); // Проверка существования реактива

        ReagentBatch batch = new ReagentBatch();
        batch.setReagentId(reagentId);
        batch.setLabel(label);
        batch.setQuantityCurrent(quantity);
        batch.setUnit(unit);
        batch.setLocation(location);
        batch.setExpiresAt(expiresAt);

        validator.validate(batch);

        Instant now = Instant.now();
        batch.setCreatedAt(now);
        batch.setUpdatedAt(now);

        return batchRepository.save(batch);
    }

    public List<ReagentBatch> listBatches(long reagentId, boolean activeOnly) {
        reagentService.getById(reagentId);
        return batchRepository.findAllByReagentId(reagentId).stream()
                .filter(b -> !activeOnly || b.getStatus() == BatchStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public List<ReagentBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    public ReagentBatch getById(long id) {
        ReagentBatch b = batchRepository.findById(id);
        if (b == null) {
            throw new ValidationException("batch не найден");
        }
        return b;
    }

    public void updateBatch(long id, String field, String value, Instant parsedDate) {
        ReagentBatch batch = getById(id);

        switch (field.toLowerCase()) {
            case "location":
                if (value == null || value.trim().isEmpty()) throw new ValidationException("location не может быть пустым");
                batch.setLocation(value);
                break;
            case "label":
                if (value == null || value.trim().isEmpty()) throw new ValidationException("label не может быть пустым");
                batch.setLabel(value);
                break;
            case "expiresat":
                batch.setExpiresAt(parsedDate);
                break;
            case "status":
                try {
                    batch.setStatus(BatchStatus.valueOf(value.toUpperCase()));
                } catch (Exception e) {
                    throw new ValidationException("статус только ACTIVE или ARCHIVED");
                }
                break;
            default:
                throw new ValidationException("нельзя менять поле '" + field + "'");
        }

        validator.validate(batch);
        batch.setUpdatedAt(Instant.now());
        batchRepository.save(batch);
    }

    public void archiveBatch(long id) {
        ReagentBatch batch = getById(id);
        if (batch.getStatus() == BatchStatus.ARCHIVED) {
            throw new ValidationException("batch уже ARCHIVED");
        }
        batch.setStatus(BatchStatus.ARCHIVED);
        batch.setUpdatedAt(Instant.now());
        batchRepository.save(batch);
    }
}