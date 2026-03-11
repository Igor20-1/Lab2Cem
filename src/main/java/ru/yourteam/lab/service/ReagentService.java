package ru.yourteam.lab.service;

import ru.yourteam.lab.domain.Reagent;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.ReagentRepository;
import ru.yourteam.lab.validation.ReagentValidator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ReagentService {
    private final ReagentRepository repository;
    private final ReagentValidator validator;

    // Конструктор внедряет зависимости (DI)
    public ReagentService(ReagentRepository repository, ReagentValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public Reagent createReagent(String name, String formula, String cas, String hazardClass) {
        Reagent reagent = new Reagent();
        reagent.setName(name);
        reagent.setFormula(formula != null && !formula.isEmpty() ? formula : null);
        reagent.setCas(cas != null && !cas.isEmpty() ? cas : null);
        reagent.setHazardClass(hazardClass != null && !hazardClass.isEmpty() ? hazardClass : null);

        // 1. Валидация перед сохранением
        validator.validate(reagent);

        // 2. Установка системных полей
        Instant now = Instant.now();
        reagent.setCreatedAt(now);
        reagent.setUpdatedAt(now);

        // 3. Сохранение
        return repository.save(reagent);
    }

    public List<Reagent> searchReagents(String query) {
        List<Reagent> all = repository.findAll();
        if (query == null || query.trim().isEmpty()) {
            return all;
        }

        if (query.length() > 64) {
            throw new ValidationException("запрос слишком длинный (макс. 64)");
        }

        String lowerQuery = query.toLowerCase();
        List<Reagent> result = new ArrayList<>();
        for (Reagent r : all) {
            if (r.getName().toLowerCase().contains(lowerQuery) ||
                    (r.getFormula() != null && r.getFormula().toLowerCase().contains(lowerQuery)) ||
                    (r.getCas() != null && r.getCas().toLowerCase().contains(lowerQuery))) {
                result.add(r);
            }
        }
        return result;
    }

    public Reagent getById(long id) {
        Reagent r = repository.findById(id);
        if (r == null) {
            throw new ValidationException("reagent с id=" + id + " не найден");
        }
        return r;
    }
}