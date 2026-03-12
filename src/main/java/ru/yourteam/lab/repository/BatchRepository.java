// PATH: .\src\main\java\ru\yourteam\lab\repository\BatchRepository.java
package ru.yourteam.lab.repository;
import ru.yourteam.lab.domain.ReagentBatch;
import java.util.List;

public interface BatchRepository extends CrudRepository<ReagentBatch> {
    List<ReagentBatch> findAllByReagentId(long reagentId);
}