package ru.yourteam.lab.cli;

import ru.yourteam.lab.domain.*;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.BatchRepository;
import ru.yourteam.lab.repository.MoveRepository;
import ru.yourteam.lab.repository.ReagentRepository;
import ru.yourteam.lab.service.BatchService;
import ru.yourteam.lab.service.MoveService;
import ru.yourteam.lab.service.ReagentService;
import ru.yourteam.lab.validation.BatchValidator;
import ru.yourteam.lab.validation.MoveValidator;
import ru.yourteam.lab.validation.ReagentValidator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CliRunner {
    private final Scanner scanner;
    private final ReagentService reagentService;
    private final BatchService batchService;
    private final MoveService moveService;

    public CliRunner() {
        this.scanner = new Scanner(System.in);
        ReagentRepository reagentRepo = new ReagentRepository();
        BatchRepository batchRepo = new BatchRepository();
        MoveRepository moveRepo = new MoveRepository();

        this.reagentService = new ReagentService(reagentRepo, new ReagentValidator());
        this.batchService = new BatchService(batchRepo, new BatchValidator(), reagentService);
        this.moveService = new MoveService(moveRepo, new MoveValidator(), batchService, batchRepo);
    }

    public void run() {
        System.out.println("Lab 1 (Domain 2) started. Type 'help' for commands or 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("exit")) break;

            List<String> args = CommandParser.parseArgs(line);
            try {
                routeCommand(args);
            } catch (ValidationException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Критическая ошибка: " + e.getMessage());
            }
        }
    }

    private void routeCommand(List<String> args) {
        String command = args.get(0).toLowerCase();
        switch (command) {
            case "reag_add": handleReagAdd(); break;
            case "reag_list": handleReagList(args); break;
            case "batch_add": handleBatchAdd(args); break;
            case "batch_list": handleBatchList(args); break;
            case "batch_show": handleBatchShow(args); break;
            case "batch_update": handleBatchUpdate(args); break;
            case "batch_archive": handleBatchArchive(args); break;
            case "move_add": handleMoveAdd(args); break;
            case "move_list": handleMoveList(args); break;
            case "stock_report": handleStockReport(args); break;
            case "help":
                System.out.println("Команды: reag_add, reag_list, batch_add, batch_list, batch_show, batch_update, batch_archive, move_add, move_list, stock_report");
                break;
            default:
                throw new ValidationException("неизвестная команда '" + command + "'");
        }
    }

    // --- REAGENT COMMANDS ---
    private void handleReagAdd() {
        System.out.print("Название: ");
        String name = scanner.nextLine().trim();
        System.out.print("Формула: ");
        String formula = scanner.nextLine().trim();
        System.out.print("CAS (можно пусто): ");
        String cas = scanner.nextLine().trim();
        System.out.print("Класс опасности (можно пусто): ");
        String hazard = scanner.nextLine().trim();

        Reagent created = reagentService.createReagent(name, formula, cas, hazard);
        System.out.println("OK reagent_id=" + created.getId());
    }

    private void handleReagList(List<String> args) {
        String query = (args.size() >= 3 && args.get(1).equals("--q")) ? args.get(2) : null;
        List<Reagent> list = reagentService.searchReagents(query);
        System.out.printf("%-4s %-20s %-15s %-15s%n", "ID", "Name", "Formula", "CAS");
        for (Reagent r : list) {
            System.out.printf("%-4d %-20s %-15s %-15s%n", r.getId(), r.getName(),
                    r.getFormula() != null ? r.getFormula() : "", r.getCas() != null ? r.getCas() : "");
        }
    }

    // --- BATCH COMMANDS ---
    private void handleBatchAdd(List<String> args) {
        if (args.size() < 2) throw new ValidationException("укажите reagent_id");
        long reagentId = parseLong(args.get(1));

        System.out.print("Номер партии (label): ");
        String label = scanner.nextLine().trim();

        System.out.print("Начальное количество: ");
        double qty = parseDouble(scanner.nextLine().trim());

        System.out.print("Единицы (g|mL): ");
        String unitStr = scanner.nextLine().trim().toUpperCase();
        BatchUnit unit;
        try {
            unit = BatchUnit.valueOf(unitStr);
        } catch (Exception e) {
            throw new ValidationException("единицы только g или mL");
        }

        System.out.print("Где хранится: ");
        String location = scanner.nextLine().trim();

        System.out.print("Годен до (YYYY-MM-DD, можно пусто): ");
        Instant expires = parseDateSafe(scanner.nextLine().trim());

        ReagentBatch batch = batchService.createBatch(reagentId, label, qty, unit, location, expires);
        System.out.println("OK batch_id=" + batch.getId());
    }

    private void handleBatchList(List<String> args) {
        if (args.size() < 2) throw new ValidationException("укажите reagent_id");
        long reagentId = parseLong(args.get(1));
        boolean activeOnly = args.contains("--active");

        List<ReagentBatch> list = batchService.listBatches(reagentId, activeOnly);
        System.out.printf("%-4s %-15s %-7s %-5s %-15s %-10s%n", "ID", "Label", "Qty", "Unit", "Location", "Status");
        for (ReagentBatch b : list) {
            System.out.printf("%-4d %-15s %-7.1f %-5s %-15s %-10s%n",
                    b.getId(), b.getLabel(), b.getQuantityCurrent(), b.getUnit(), b.getLocation(), b.getStatus());
        }
    }

    private void handleBatchShow(List<String> args) {
        if (args.size() < 2) throw new ValidationException("укажите batch_id");
        ReagentBatch b = batchService.getById(parseLong(args.get(1)));
        Reagent r = reagentService.getById(b.getReagentId());

        System.out.println("Batch #" + b.getId());
        System.out.println("reagent: " + r.getName());
        System.out.println("label: " + b.getLabel());
        System.out.println("qty_current: " + b.getQuantityCurrent() + " " + b.getUnit().name().toLowerCase());
        System.out.println("location: " + b.getLocation());
        System.out.println("status: " + b.getStatus());
    }

    private void handleBatchUpdate(List<String> args) {
        if (args.size() < 3) throw new ValidationException("формат: batch_update <id> field=value");
        long batchId = parseLong(args.get(1));

        String[] pair = args.get(2).split("=", 2);
        if (pair.length != 2) throw new ValidationException("неверный формат field=value");

        String field = pair[0];
        String value = pair[1];
        Instant parsedDate = field.equalsIgnoreCase("expiresat") ? parseDateSafe(value) : null;

        batchService.updateBatch(batchId, field, value, parsedDate);
        System.out.println("OK");
    }

    private void handleBatchArchive(List<String> args) {
        if (args.size() < 2) throw new ValidationException("укажите batch_id");
        long batchId = parseLong(args.get(1));
        batchService.archiveBatch(batchId);
        System.out.println("OK batch " + batchId + " archived");
    }

    // --- MOVE COMMANDS ---
    private void handleMoveAdd(List<String> args) {
        if (args.size() < 2) throw new ValidationException("укажите batch_id");
        long batchId = parseLong(args.get(1));

        System.out.print("Тип (IN/OUT/DISCARD): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        StockMoveType type;
        try {
            type = StockMoveType.valueOf(typeStr);
        } catch (Exception e) {
            throw new ValidationException("тип только IN, OUT или DISCARD");
        }

        System.out.print("Количество: ");
        double qty = parseDouble(scanner.nextLine().trim());

        System.out.print("Причина (можно пусто): ");
        String reason = scanner.nextLine().trim();

        StockMove move = moveService.makeMove(batchId, type, qty, reason);
        System.out.println("OK move_id=" + move.getId());
    }

    private void handleMoveList(List<String> args) {
        if (args.size() < 2) throw new ValidationException("укажите batch_id");
        long batchId = parseLong(args.get(1));
        int limit = 0;

        if (args.size() >= 4 && args.get(2).equals("--last")) {
            limit = (int) parseLong(args.get(3));
        }

        List<StockMove> list = moveService.listMoves(batchId, limit);
        System.out.printf("%-4s %-8s %-7s %-5s %-20s%n", "ID", "Type", "Qty", "Unit", "Reason");
        for (StockMove m : list) {
            System.out.printf("%-4d %-8s %-7.1f %-5s %-20s%n",
                    m.getId(), m.getType(), m.getQuantity(), m.getUnit(), m.getReason() != null ? m.getReason() : "-");
        }
    }

    private void handleStockReport(List<String> args) {
        Instant beforeDate = null;
        if (args.size() >= 3 && args.get(1).equals("--expires-before")) {
            beforeDate = parseDateSafe(args.get(2));
            if (beforeDate == null) throw new ValidationException("дата должна быть YYYY-MM-DD");
        }

        final Instant filterDate = beforeDate;
        List<ReagentBatch> all = batchService.getAllBatches();

        System.out.printf("%-4s %-15s %-20s %-15s%n", "Batch", "Label", "Reagent", "Status");
        for (ReagentBatch b : all) {
            if (filterDate != null && (b.getExpiresAt() == null || !b.getExpiresAt().isBefore(filterDate))) {
                continue; // Пропускаем, если дата больше или не указана
            }
            Reagent r = reagentService.getById(b.getReagentId());
            System.out.printf("%-4d %-15s %-20s %-15s%n", b.getId(), b.getLabel(), r.getName(), b.getStatus());
        }
    }

    // --- UTILS ---
    private long parseLong(String str) {
        try { return Long.parseLong(str); }
        catch (NumberFormatException e) { throw new ValidationException("должно быть числом"); }
    }

    private double parseDouble(String str) {
        try { return Double.parseDouble(str); }
        catch (NumberFormatException e) { throw new ValidationException("должно быть числом"); }
    }

    private Instant parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            throw new ValidationException("дата должна быть YYYY-MM-DD");
        }
    }
}