package ru.yourteam.lab.repository;

import ru.yourteam.lab.domain.Reagent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReagentRepository {
    // LinkedHashMap гарантирует сохранение порядка добавления (Вариант 3)
    private final Map<Long, Reagent> store = new LinkedHashMap<>();
    private long idCounter = 1;

    public Reagent save(Reagent reagent) {
        if (reagent.getId() == 0) {
            reagent.setId(idCounter++);
        }
        store.put(reagent.getId(), reagent);
        return reagent;
    }

    public Reagent findById(long id) {
        return store.get(id);
    }

    public List<Reagent> findAll() {
        return new ArrayList<>(store.values());
    }
}