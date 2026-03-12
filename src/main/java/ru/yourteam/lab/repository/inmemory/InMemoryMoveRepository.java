// PATH: .\src\main\java\ru\yourteam\lab\repository\inmemory\InMemoryMoveRepository.java
package ru.yourteam.lab.repository.inmemory;

import ru.yourteam.lab.domain.StockMove;
import ru.yourteam.lab.repository.MoveRepository;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryMoveRepository extends AbstractInMemoryRepository<StockMove> implements MoveRepository {
    @Override
    public List<StockMove> findAllByBatchId(long batchId) {
        return store.values().stream()
                .filter(move -> move.getBatchId() == batchId)
                .collect(Collectors.toList());
    }
}