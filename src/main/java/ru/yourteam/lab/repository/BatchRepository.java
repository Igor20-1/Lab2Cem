package ru.yourteam.lab.repository;

import ru.yourteam.lab.domain.ReagentBatch;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BatchRepository {
    private final Map<Long, ReagentBatch> store = new LinkedHashMap<>();
    private long idCounter = 1;

    public ReagentBatch save(ReagentBatch batch) {
        if (batch.getId() == 0) {
            batch.setId(idCounter++);
        }
        store.put(batch.getId(), batch);
        return batch;
    }

    public ReagentBatch findById(long id) {
        return store.get(id);
    }

    public List<ReagentBatch> findAllByReagentId(long reagentId) {
        List<ReagentBatch> result = new ArrayList<>();
        for (ReagentBatch batch : store.values()) {
            if (batch.getReagentId() == reagentId) {
                result.add(batch);
            }
        }
        return result;
    }
    public List<ReagentBatch> findAll() {
        return new ArrayList<>(store.values());
    }
}