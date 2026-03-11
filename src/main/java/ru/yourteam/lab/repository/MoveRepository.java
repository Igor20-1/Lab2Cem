package ru.yourteam.lab.repository;

import ru.yourteam.lab.domain.StockMove;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MoveRepository {
    private final Map<Long, StockMove> store = new LinkedHashMap<>();
    private long idCounter = 1;

    public StockMove save(StockMove move) {
        if (move.getId() == 0) {
            move.setId(idCounter++);
        }
        store.put(move.getId(), move);
        return move;
    }

    public List<StockMove> findAllByBatchId(long batchId) {
        List<StockMove> result = new ArrayList<>();
        for (StockMove move : store.values()) {
            if (move.getBatchId() == batchId) {
                result.add(move);
            }
        }
        return result;
    }
}