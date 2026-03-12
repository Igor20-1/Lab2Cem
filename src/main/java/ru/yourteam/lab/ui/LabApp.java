package ru.yourteam.lab.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.yourteam.lab.domain.*;
import ru.yourteam.lab.exception.ValidationException;
import ru.yourteam.lab.repository.*;
import ru.yourteam.lab.repository.inmemory.InMemoryBatchRepository;
import ru.yourteam.lab.repository.inmemory.InMemoryMoveRepository;
import ru.yourteam.lab.repository.inmemory.InMemoryReagentRepository;
import ru.yourteam.lab.service.*;
import ru.yourteam.lab.storage.CsvStorage;
import ru.yourteam.lab.validation.*;

import java.time.Instant;

public class LabApp extends Application {
    private ReagentService reagentService;
    private BatchService batchService;
    private CsvStorage csvStorage;

    private FlowPane reagentsPane;
    private FlowPane batchesPane;

    @Override
    public void init() {
        // Инициализируем всю логику и хранилища
        ReagentRepository reagentRepo = new InMemoryReagentRepository();
        BatchRepository batchRepo = new InMemoryBatchRepository();
        MoveRepository moveRepo = new InMemoryMoveRepository();

        this.reagentService = new ReagentService(reagentRepo, new ReagentValidator());
        this.batchService = new BatchService(batchRepo, new BatchValidator(), reagentService);
        MoveService moveService = new MoveService(moveRepo, new MoveValidator(), batchService, batchRepo);
        this.csvStorage = new CsvStorage(reagentRepo, batchRepo, moveRepo);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Лаборатория: Реактивы и Склад (ЛР2)");

        // --- Верхняя панель (Toolbar) ---
        Button btnRefresh = new Button("Refresh");
        Button btnAddReagent = new Button("+ Реактив");
        Button btnAddBatch = new Button("+ Партия");
        Button btnSave = new Button("Сохранить (CSV)");
        Button btnLoad = new Button("Загрузить (CSV)");

        ToolBar toolBar = new ToolBar(btnRefresh, new Separator(), btnAddReagent, btnAddBatch, new Separator(), btnSave, btnLoad);

        // --- Центральная панель (Вкладки с карточками) ---
        TabPane tabPane = new TabPane();

        Tab tabReagents = new Tab("Реактивы");
        tabReagents.setClosable(false);
        reagentsPane = new FlowPane(10, 10);
        reagentsPane.setPadding(new Insets(10));
        ScrollPane scrollReagents = new ScrollPane(reagentsPane);
        scrollReagents.setFitToWidth(true);
        tabReagents.setContent(scrollReagents);

        Tab tabBatches = new Tab("Партии (Бутылки)");
        tabBatches.setClosable(false);
        batchesPane = new FlowPane(10, 10);
        batchesPane.setPadding(new Insets(10));
        ScrollPane scrollBatches = new ScrollPane(batchesPane);
        scrollBatches.setFitToWidth(true);
        tabBatches.setContent(scrollBatches);

        tabPane.getTabs().addAll(tabReagents, tabBatches);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(tabPane);

        // --- Обработчики кнопок ---
        btnRefresh.setOnAction(e -> refreshUI());

        btnAddReagent.setOnAction(e -> showAddReagentDialog());
        btnAddBatch.setOnAction(e -> showAddBatchDialog());

        btnSave.setOnAction(e -> {
            try {
                csvStorage.save("data.csv");
                showInfo("Успех", "Данные сохранены в data.csv");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        btnLoad.setOnAction(e -> {
            try {
                csvStorage.load("data.csv");
                showInfo("Успех", "Данные загружены из data.csv. Нажмите Refresh для обновления экрана.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshUI(); // Загружаем пустые экраны при старте
    }

    // --- Логика обновления (согласно требованию "ручного обновления") ---
    private void refreshUI() {
        reagentsPane.getChildren().clear();
        for (Reagent r : reagentService.searchReagents(null)) {
            reagentsPane.getChildren().add(createReagentCard(r));
        }

        batchesPane.getChildren().clear();
        for (ReagentBatch b : batchService.getAllBatches()) {
            batchesPane.getChildren().add(createBatchCard(b));
        }
    }

    // --- Создание Карточек ---
    private VBox createReagentCard(Reagent r) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #aaa; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
        card.setPrefWidth(220);

        Label lblName = new Label(r.getName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        card.getChildren().addAll(
                new Label("ID: " + r.getId()),
                lblName,
                new Label("Формула: " + (r.getFormula() != null ? r.getFormula() : "-")),
                new Label("CAS: " + (r.getCas() != null ? r.getCas() : "-"))
        );

        card.setOnMouseClicked(e -> showInfo("Детали реактива", "Выбран реактив: " + r.getName() + "\nКласс опасности: " + r.getHazardClass()));
        return card;
    }

    private VBox createBatchCard(ReagentBatch b) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #aaa; -fx-border-radius: 5; -fx-background-color: #eef7ea;");
        card.setPrefWidth(220);

        Label lblLabel = new Label(b.getLabel());
        lblLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        card.getChildren().addAll(
                new Label("Batch ID: " + b.getId() + " (Reagent: " + b.getReagentId() + ")"),
                lblLabel,
                new Label("Остаток: " + b.getQuantityCurrent() + " " + b.getUnit()),
                new Label("Место: " + b.getLocation()),
                new Label("Статус: " + b.getStatus())
        );

        card.setOnMouseClicked(e -> showInfo("Детали партии", "Партия: " + b.getLabel() + "\nЛокация: " + b.getLocation()));
        return card;
    }

    // --- Диалоги добавления ---
    private void showAddReagentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавление реактива");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField fldName = new TextField();
        TextField fldFormula = new TextField();
        TextField fldCas = new TextField();

        grid.add(new Label("Название*:"), 0, 0); grid.add(fldName, 1, 0);
        grid.add(new Label("Формула:"), 0, 1); grid.add(fldFormula, 1, 1);
        grid.add(new Label("CAS:"), 0, 2); grid.add(fldCas, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    reagentService.createReagent(fldName.getText(), fldFormula.getText(), fldCas.getText(), null);
                    showInfo("Успех", "Реактив создан! Нажмите Refresh, чтобы увидеть его.");
                } catch (ValidationException e) {
                    showError(e.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void showAddBatchDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавление партии");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField fldReagentId = new TextField();
        TextField fldLabel = new TextField();
        TextField fldQty = new TextField();
        ComboBox<BatchUnit> cbUnit = new ComboBox<>();
        cbUnit.getItems().addAll(BatchUnit.values());
        cbUnit.setValue(BatchUnit.G);
        TextField fldLoc = new TextField();

        grid.add(new Label("ID Реактива*:"), 0, 0); grid.add(fldReagentId, 1, 0);
        grid.add(new Label("Метка (Label)*:"), 0, 1); grid.add(fldLabel, 1, 1);
        grid.add(new Label("Количество*:"), 0, 2); grid.add(fldQty, 1, 2);
        grid.add(new Label("Единицы*:"), 0, 3); grid.add(cbUnit, 1, 3);
        grid.add(new Label("Локация*:"), 0, 4); grid.add(fldLoc, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    long rId = Long.parseLong(fldReagentId.getText());
                    double qty = Double.parseDouble(fldQty.getText());
                    batchService.createBatch(rId, fldLabel.getText(), qty, cbUnit.getValue(), fldLoc.getText(), null);
                    showInfo("Успех", "Партия добавлена! Нажмите Refresh, чтобы увидеть её.");
                } catch (NumberFormatException e) {
                    showError("ID и Количество должны быть числами.");
                } catch (ValidationException e) {
                    showError(e.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // --- Утилиты вывода ошибок (согласно требованиям ЛР2) ---
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка валидации");
        alert.setHeaderText("Что-то пошло не так:");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}