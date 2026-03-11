package ru.yourteam.lab.validation;

import ru.yourteam.lab.domain.Reagent;
import ru.yourteam.lab.exception.ValidationException;

public class ReagentValidator {
    public void validate(Reagent reagent) {
        if (reagent.getName() == null || reagent.getName().trim().isEmpty()) {
            throw new ValidationException("название не может быть пустым");
        }
        if (reagent.getName().length() > 128) {
            throw new ValidationException("название слишком длинное (макс. 128)");
        }
        if (reagent.getFormula() != null && reagent.getFormula().length() > 32) {
            throw new ValidationException("формула слишком длинная (макс. 32)");
        }
        if (reagent.getCas() != null && reagent.getCas().length() > 32) {
            throw new ValidationException("CAS номер слишком длинный (макс. 32)");
        }
        if (reagent.getHazardClass() != null && reagent.getHazardClass().length() > 32) {
            throw new ValidationException("класс опасности слишком длинный (макс. 32)");
        }
    }
}