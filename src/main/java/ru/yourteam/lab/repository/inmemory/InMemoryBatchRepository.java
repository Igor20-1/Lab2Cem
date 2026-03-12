// PATH: .\src\main\java\ru\yourteam\lab\repository\inmemory\InMemoryBatchRepository.java
package ru.yourteam.lab.repository.inmemory;

import ru.yourteam.lab.domain.ReagentBatch;
import ru.yourteam.lab.repository.BatchRepository;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryBatchRepository extends AbstractInMemoryRepository<ReagentBatch> implements BatchRepository {
    @Override
    public List<ReagentBatch> findAllByReagentId(long reagentId) {
        return store.values().stream()
                .filter(batch -> batch.getReagentId() == reagentId)
                .collect(Collectors.toList());
    }
}