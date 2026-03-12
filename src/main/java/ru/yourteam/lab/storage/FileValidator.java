package ru.yourteam.lab.storage;

import ru.yourteam.lab.domain.Reagent;
import ru.yourteam.lab.domain.ReagentBatch;
import ru.yourteam.lab.domain.StockMove;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.validation.BatchValidator;
import ru.yourteam.lab.validation.MoveValidator;
import ru.yourteam.lab.validation.ReagentValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileValidator {
    private final ReagentValidator reagentValidator = new ReagentValidator();
    private final BatchValidator batchValidator = new BatchValidator();
    private final MoveValidator moveValidator = new MoveValidator();

    public void validateSnapshot(List<Reagent> reagents, List<ReagentBatch> batches, List<StockMove> moves) {
        Set<Long> reagentIds = new HashSet<>();
        Set<Long> batchIds = new HashSet<>();
        Set<Long> moveIds = new HashSet<>();

        // 1. Проверяем реактивы
        for (Reagent r : reagents) {
            if (!reagentIds.add(r.getId())) {
                throw new ValidationException("Ошибка загрузки: дубликат ID реактива: " + r.getId());
            }
            reagentValidator.validate(r);
        }

        // 2. Проверяем бутылки и внешние ключи (reagentId)
        for (ReagentBatch b : batches) {
            if (!batchIds.add(b.getId())) {
                throw new ValidationException("Ошибка загрузки: дубликат ID партии: " + b.getId());
            }
            if (!reagentIds.contains(b.getReagentId())) {
                throw new ValidationException("Ошибка загрузки: партия " + b.getId() + " ссылается на несуществующий reagentId=" + b.getReagentId());
            }
            batchValidator.validate(b);
        }

        // 3. Проверяем движения и внешние ключи (batchId)
        for (StockMove m : moves) {
            if (!moveIds.add(m.getId())) {
                throw new ValidationException("Ошибка загрузки: дубликат ID движения: " + m.getId());
            }
            if (!batchIds.contains(m.getBatchId())) {
                throw new ValidationException("Ошибка загрузки: движение " + m.getId() + " ссылается на несуществующую партию batchId=" + m.getBatchId());
            }
            moveValidator.validate(m);
        }
    }
}