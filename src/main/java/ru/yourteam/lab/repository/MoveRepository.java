// PATH: .\src\main\java\ru\yourteam\lab\repository\MoveRepository.java
package ru.yourteam.lab.repository;
import ru.yourteam.lab.domain.StockMove;
import java.util.List;

public interface MoveRepository extends CrudRepository<StockMove> {
    List<StockMove> findAllByBatchId(long batchId);
}