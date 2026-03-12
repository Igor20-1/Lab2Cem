package ru.yourteam.lab.storage;

import ru.yourteam.lab.domain.*;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.BatchRepository;
import ru.yourteam.lab.repository.MoveRepository;
import ru.yourteam.lab.repository.ReagentRepository;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CsvStorage {
    private final ReagentRepository reagentRepo;
    private final BatchRepository batchRepo;
    private final MoveRepository moveRepo;
    private final FileValidator fileValidator;

    public CsvStorage(ReagentRepository reagentRepo, BatchRepository batchRepo, MoveRepository moveRepo) {
        this.reagentRepo = reagentRepo;
        this.batchRepo = batchRepo;
        this.moveRepo = moveRepo;
        this.fileValidator = new FileValidator();
    }

    public void save(String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            writer.println("[REAGENTS]");
            for (Reagent r : reagentRepo.findAll()) {
                writer.printf("%d;%s;%s;%s;%s;%s;%s;%s%n",
                        r.getId(), safeStr(r.getName()), safeStr(r.getFormula()), safeStr(r.getCas()),
                        safeStr(r.getHazardClass()), safeStr(r.getOwnerUsername()),
                        safeInst(r.getCreatedAt()), safeInst(r.getUpdatedAt()));
            }

            writer.println("[BATCHES]");
            for (ReagentBatch b : batchRepo.findAll()) {
                writer.printf("%d;%d;%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                        b.getId(), b.getReagentId(), safeStr(b.getLabel()), b.getQuantityCurrent(),
                        b.getUnit().name(), safeStr(b.getLocation()), safeInst(b.getExpiresAt()),
                        b.getStatus().name(), safeStr(b.getOwnerUsername()), safeInst(b.getCreatedAt()), safeInst(b.getUpdatedAt()));
            }

            writer.println("[MOVES]");
            for (ReagentBatch b : batchRepo.findAll()) { // Для движения берем все движения всех бутылок
                for (StockMove m : moveRepo.findAllByBatchId(b.getId())) {
                    writer.printf("%d;%d;%s;%s;%s;%s;%s;%s;%s%n",
                            m.getId(), m.getBatchId(), m.getType().name(), m.getQuantity(),
                            m.getUnit().name(), safeStr(m.getReason()), safeStr(m.getOwnerUsername()),
                            safeInst(m.getMovedAt()), safeInst(m.getCreatedAt()));
                }
            }
        } catch (IOException e) {
            throw new ValidationException("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    public void load(String path) {
        File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            throw new ValidationException("Файл не существует или недоступен для чтения");
        }

        List<Reagent> loadedReagents = new ArrayList<>();
        List<ReagentBatch> loadedBatches = new ArrayList<>();
        List<StockMove> loadedMoves = new ArrayList<>();
        String currentSection = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("[")) {
                    currentSection = line;
                    continue;
                }

                String[] p = line.split(";", -1);
                try {
                    switch (currentSection) {
                        case "[REAGENTS]":
                            Reagent r = new Reagent();
                            r.setId(Long.parseLong(p[0]));
                            r.setName(parseStr(p[1]));
                            r.setFormula(parseStr(p[2]));
                            r.setCas(parseStr(p[3]));
                            r.setHazardClass(parseStr(p[4]));
                            r.setOwnerUsername(parseStr(p[5]));
                            r.setCreatedAt(parseInst(p[6]));
                            r.setUpdatedAt(parseInst(p[7]));
                            loadedReagents.add(r);
                            break;
                        case "[BATCHES]":
                            ReagentBatch b = new ReagentBatch();
                            b.setId(Long.parseLong(p[0]));
                            b.setReagentId(Long.parseLong(p[1]));
                            b.setLabel(parseStr(p[2]));
                            b.setQuantityCurrent(Double.parseDouble(p[3]));
                            b.setUnit(BatchUnit.valueOf(p[4]));
                            b.setLocation(parseStr(p[5]));
                            b.setExpiresAt(parseInst(p[6]));
                            b.setStatus(BatchStatus.valueOf(p[7]));
                            b.setOwnerUsername(parseStr(p[8]));
                            b.setCreatedAt(parseInst(p[9]));
                            b.setUpdatedAt(parseInst(p[10]));
                            loadedBatches.add(b);
                            break;
                        case "[MOVES]":
                            StockMove m = new StockMove();
                            m.setId(Long.parseLong(p[0]));
                            m.setBatchId(Long.parseLong(p[1]));
                            m.setType(StockMoveType.valueOf(p[2]));
                            m.setQuantity(Double.parseDouble(p[3]));
                            m.setUnit(BatchUnit.valueOf(p[4]));
                            m.setReason(parseStr(p[5]));
                            m.setOwnerUsername(parseStr(p[6]));
                            m.setMovedAt(parseInst(p[7]));
                            m.setCreatedAt(parseInst(p[8]));
                            loadedMoves.add(m);
                            break;
                    }
                } catch (Exception ex) {
                    throw new ValidationException("Ошибка парсинга строки: " + line + ". Причина: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ValidationException("Ошибка при чтении файла: " + e.getMessage());
        }

        // Атомарность: сначала проверяем
        fileValidator.validateSnapshot(loadedReagents, loadedBatches, loadedMoves);

        // Если дошли сюда, ошибок нет. Заменяем данные в памяти.
        reagentRepo.replaceAll(loadedReagents);
        batchRepo.replaceAll(loadedBatches);
        moveRepo.replaceAll(loadedMoves);
    }

    // Утилиты для безопасной записи и чтения
    private String safeStr(String s) { return s == null ? "" : s; }
    private String safeInst(Instant i) { return i == null ? "" : i.toString(); }
    private String parseStr(String s) { return s.isEmpty() ? null : s; }
    private Instant parseInst(String s) { return s.isEmpty() ? null : Instant.parse(s); }
}