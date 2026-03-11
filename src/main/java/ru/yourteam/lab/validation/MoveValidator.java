package ru.yourteam.lab.validation;

import ru.yourteam.lab.domain.StockMove;
import ru.yourteam.lab.exception.ValidationException;

public class MoveValidator {
    public void validate(StockMove move) {
        if (move.getQuantity() <= 0) {
            throw new ValidationException("количество должно быть > 0");
        }
        if (move.getType() == null) {
            throw new ValidationException("тип движения не указан");
        }
        if (move.getReason() != null && move.getReason().length() > 128) {
            throw new ValidationException("причина слишком длинная (макс. 128)");
        }
    }
}