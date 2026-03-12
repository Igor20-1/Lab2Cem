// PATH: .\src\main\java\ru\yourteam\lab\repository\inmemory\AbstractInMemoryRepository.java
package ru.yourteam.lab.repository.inmemory;

import ru.yourteam.lab.domain.Identifiable;
import ru.yourteam.lab.repository.CrudRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractInMemoryRepository<T extends Identifiable> implements CrudRepository<T> {
    protected final Map<Long, T> store = new LinkedHashMap<>();
    protected long idCounter = 1;

    @Override
    public T save(T item) {
        if (item.getId() == 0) {
            item.setId(idCounter++);
        }
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public T findById(long id) {
        return store.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void replaceAll(List<T> items) {
        store.clear();
        long maxId = 0;
        for (T item : items) {
            store.put(item.getId(), item);
            if (item.getId() > maxId) {
                maxId = item.getId();
            }
        }
        idCounter = maxId + 1;
    }
}