package ui;

import detection.SampleDetectionPrediction;
import detection.SampleDetector;
import domain.Experiment;
import domain.MeasurementParam;
import domain.Result;
import domain.Run;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import manager.CollectionManager;
import storage.LocalDataStorage;
import user.AuthService;
import user.UserRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SampleDetectionApp extends Application {
    private final CollectionManager manager = new CollectionManager();
    private final UserRepository userRepository = new UserRepository();
    private final AuthService authService = new AuthService(userRepository);
    private final LocalDataStorage localStorage = new LocalDataStorage(Path.of("sample-data.bin"));

    private final ObservableList<Experiment> experiments = FXCollections.observableArrayList();
    private final ObservableList<Run> runs = FXCollections.observableArrayList();
    private final ObservableList<Result> results = FXCollections.observableArrayList();
    private final ObservableList<ProbabilityRow> probabilities = FXCollections.observableArrayList();

    private final TableView<Experiment> experimentTable = new TableView<>();
    private final TableView<Run> runTable = new TableView<>();
    private final TableView<Result> resultTable = new TableView<>();
    private final TableView<ProbabilityRow> probabilityTable = new TableView<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    private Stage stage;
    private TextField experimentNameField;
    private TextArea experimentDescriptionArea;
    private ComboBox<Experiment> experimentPicker;
    private TextField runNameField;
    private ComboBox<Run> detectionRunBox;
    private TextField imagePathField;
    private TextField pythonField;
    private ImageView imageView;
    private Label predictionLabel;
    private Label confidenceLabel;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    private Button analyzeButton;
    private Path selectedImage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        showAuthWindow(!authService.hasUsers());
    }

    private void showAuthWindow(boolean registerFirst) {
        Label title = new Label("Детекция образца");
        title.getStyleClass().add("auth-title");
        TabPane tabs = new TabPane(createLoginTab(), createRegisterTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getSelectionModel().select(registerFirst ? 1 : 0);
        VBox root = new VBox(14, title, tabs);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("auth-root");
        Scene scene = new Scene(root, 420, 300);
        applyStyles(scene);
        stage.setTitle("Детекция образца");
        stage.setScene(scene);
        stage.show();
    }

    private Tab createLoginTab() {
        TextField login = new TextField();
        PasswordField password = new PasswordField();
        Label message = new Label();
        message.getStyleClass().add("message-label");
        Button action = new Button("Войти");
        action.setDefaultButton(true);
        action.setOnAction(event -> {
            try {
                authService.login(login.getText(), password.getText());
                showMainWindow();
            } catch (Exception e) {
                message.setText(e.getMessage());
            }
        });
        return new Tab("Вход", authForm(login, password, action, message));
    }

    private Tab createRegisterTab() {
        TextField login = new TextField();
        PasswordField password = new PasswordField();
        Label message = new Label();
        message.getStyleClass().add("message-label");
        Button action = new Button("Создать пользователя");
        action.setDefaultButton(true);
        action.setOnAction(event -> {
            try {
                authService.register(login.getText(), password.getText());
                authService.login(login.getText(), password.getText());
                showMainWindow();
            } catch (Exception e) {
                message.setText(e.getMessage());
            }
        });
        return new Tab("Регистрация", authForm(login, password, action, message));
    }

    private VBox authForm(TextField login, PasswordField password, Button action, Label message) {
        login.setPromptText("Логин");
        password.setPromptText("Пароль");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Логин"), 0, 0);
        grid.add(login, 1, 0);
        grid.add(new Label("Пароль"), 0, 1);
        grid.add(password, 1, 1);
        VBox form = new VBox(12, grid, action, message);
        form.setPadding(new Insets(14));
        form.getStyleClass().add("panel");
        return form;
    }

    private void showMainWindow() {
        statusLabel = new Label("Готово");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(20, 20);
        progressIndicator.setVisible(false);
        BorderPane root = new BorderPane();
        root.setTop(createToolbar());
        root.setCenter(createWorkspace());
        root.setBottom(createStatusBar());
        tryLoadSavedData();
        refreshData();
        Scene scene = new Scene(root, 1240, 760);
        applyStyles(scene);
        stage.setTitle("Детекция образца");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createToolbar() {
        Label title = new Label("Детекция образца");
        title.getStyleClass().add("app-title");
        Label user = new Label(authService.getCurrentUsername());
        user.getStyleClass().add("user-label");
        Button save = new Button("Сохранить");
        save.setOnAction(event -> saveData());
        Button load = new Button("Загрузить");
        load.setOnAction(event -> loadData());
        Button refresh = new Button("Обновить");
        refresh.setOnAction(event -> refreshData());
        Button logout = new Button("Выйти");
        logout.setOnAction(event -> {
            authService.logout();
            showAuthWindow(false);
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(12, title, spacer, user, save, load, refresh, logout);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.getStyleClass().add("toolbar");
        return toolbar;
    }

    private HBox createStatusBar() {
        HBox bar = new HBox(10, progressIndicator, statusLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 14, 8, 14));
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    private SplitPane createWorkspace() {
        SplitPane split = new SplitPane(createJournalPane(), createDetectionPane());
        split.setDividerPositions(0.45);
        return split;
    }

    private TabPane createJournalPane() {
        TabPane tabs = new TabPane(
                new Tab("Эксперименты", createExperimentPane()),
                new Tab("Запуски", createRunPane()),
                new Tab("Результаты", createResultPane())
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabs;
    }

    private BorderPane createExperimentPane() {
        configureExperimentTable();
        experimentNameField = new TextField();
        experimentNameField.setPromptText("Название");
        experimentDescriptionArea = new TextArea();
        experimentDescriptionArea.setPromptText("Описание");
        experimentDescriptionArea.setPrefRowCount(3);
        Button create = new Button("Создать");
        create.setOnAction(event -> createExperiment());
        Button delete = new Button("Удалить");
        delete.setOnAction(event -> deleteSelectedExperiment());
        VBox form = new VBox(8, new Label("Новый эксперимент"), experimentNameField,
                experimentDescriptionArea, new HBox(8, create, delete));
        form.setPadding(new Insets(12));
        form.getStyleClass().add("side-form");
        BorderPane pane = new BorderPane(experimentTable);
        pane.setBottom(form);
        return pane;
    }

    private BorderPane createRunPane() {
        configureRunTable();
        experimentPicker = new ComboBox<>(experiments);
        experimentPicker.setConverter(experimentConverter());
        experimentPicker.setMaxWidth(Double.MAX_VALUE);
        runNameField = new TextField();
        runNameField.setPromptText("Название запуска");
        Button create = new Button("Создать");
        create.setOnAction(event -> createRun());
        Button delete = new Button("Удалить");
        delete.setOnAction(event -> deleteSelectedRun());
        VBox form = new VBox(8, new Label("Новый запуск"), experimentPicker, runNameField,
                new HBox(8, create, delete));
        form.setPadding(new Insets(12));
        form.getStyleClass().add("side-form");
        BorderPane pane = new BorderPane(runTable);
        pane.setBottom(form);
        return pane;
    }

    private BorderPane createResultPane() {
        configureResultTable();
        return new BorderPane(resultTable);
    }

    private BorderPane createDetectionPane() {
        detectionRunBox = new ComboBox<>(runs);
        detectionRunBox.setConverter(runConverter());
        detectionRunBox.setMaxWidth(Double.MAX_VALUE);
        detectionRunBox.valueProperty().addListener((obs, oldValue, newValue) -> updateAnalyzeState());
        imagePathField = new TextField();
        imagePathField.setEditable(false);
        imagePathField.setPromptText("Изображение не выбрано");
        Button chooseImage = new Button("Выбрать изображение");
        chooseImage.setOnAction(event -> chooseImage());
        pythonField = new TextField(defaultPythonExecutable());
        pythonField.setPromptText("Автоматически или путь к python.exe");
        Button choosePython = new Button("Выбрать Python");
        choosePython.setOnAction(event -> choosePythonExecutable());
        analyzeButton = new Button("Выполнить детекцию");
        analyzeButton.setOnAction(event -> analyzeSelectedImage());
        analyzeButton.setDisable(true);

        GridPane controls = new GridPane();
        controls.setHgap(10);
        controls.setVgap(10);
        controls.setPadding(new Insets(14));
        controls.getStyleClass().add("controls");
        controls.add(new Label("Запуск"), 0, 0);
        controls.add(detectionRunBox, 1, 0, 2, 1);
        controls.add(new Label("Изображение"), 0, 1);
        controls.add(imagePathField, 1, 1);
        controls.add(chooseImage, 2, 1);
        controls.add(new Label("Python"), 0, 2);
        controls.add(pythonField, 1, 2);
        controls.add(choosePython, 2, 2);
        controls.add(analyzeButton, 3, 2);
        GridPane.setHgrow(detectionRunBox, Priority.ALWAYS);
        GridPane.setHgrow(imagePathField, Priority.ALWAYS);
        GridPane.setHgrow(pythonField, Priority.ALWAYS);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        StackPane preview = new StackPane(imageView);
        preview.setMinHeight(330);
        preview.getStyleClass().add("image-preview");
        imageView.fitWidthProperty().bind(preview.widthProperty().subtract(28));
        imageView.fitHeightProperty().bind(preview.heightProperty().subtract(28));

        predictionLabel = new Label("Результат: -");
        predictionLabel.getStyleClass().add("prediction-label");
        confidenceLabel = new Label("Уверенность: -");
        confidenceLabel.getStyleClass().add("confidence-label");
        configureProbabilityTable();
        VBox summary = new VBox(10, predictionLabel, confidenceLabel, probabilityTable);
        summary.setPadding(new Insets(14));
        summary.getStyleClass().add("summary");

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(preview);
        pane.setBottom(summary);
        return pane;
    }

    private void configureExperimentTable() {
        experimentTable.setItems(experiments);
        experimentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Experiment, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        id.setMaxWidth(72);
        TableColumn<Experiment, String> name = new TableColumn<>("Название");
        name.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        TableColumn<Experiment, String> owner = new TableColumn<>("Владелец");
        owner.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getOwnerUsername()));
        TableColumn<Experiment, String> created = new TableColumn<>("Создан");
        created.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDate(data.getValue().getCreatedAt())));
        experimentTable.getColumns().setAll(id, name, owner, created);
        experimentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null && experimentPicker != null) {
                experimentPicker.getSelectionModel().select(selected);
            }
        });
    }

    private void configureRunTable() {
        runTable.setItems(runs);
        runTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Run, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        id.setMaxWidth(72);
        TableColumn<Run, Number> experimentId = new TableColumn<>("Эксперимент");
        experimentId.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getExperimentId()));
        TableColumn<Run, String> name = new TableColumn<>("Запуск");
        name.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        TableColumn<Run, String> operator = new TableColumn<>("Оператор");
        operator.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getOperatorUsername()));
        runTable.getColumns().setAll(id, experimentId, name, operator);
        runTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null && detectionRunBox != null) {
                detectionRunBox.getSelectionModel().select(selected);
            }
        });
    }

    private void configureResultTable() {
        resultTable.setItems(results);
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Result, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        id.setMaxWidth(72);
        TableColumn<Result, Number> runId = new TableColumn<>("Запуск");
        runId.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getRunId()));
        TableColumn<Result, String> param = new TableColumn<>("Параметр");
        param.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getParam())));
        TableColumn<Result, Number> value = new TableColumn<>("Значение");
        value.setCellValueFactory(data -> new ReadOnlyDoubleWrapper(data.getValue().getValue()));
        TableColumn<Result, String> comment = new TableColumn<>("Комментарий");
        comment.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getComment()));
        resultTable.getColumns().setAll(id, runId, param, value, comment);
    }

    private void configureProbabilityTable() {
        probabilityTable.setItems(probabilities);
        probabilityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        probabilityTable.setPrefHeight(185);
        TableColumn<ProbabilityRow, String> label = new TableColumn<>("Класс");
        label.setCellValueFactory(data -> data.getValue().labelProperty());
        TableColumn<ProbabilityRow, Number> value = new TableColumn<>("Вероятность");
        value.setCellValueFactory(data -> data.getValue().probabilityProperty());
        value.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(Locale.ROOT, "%.4f", item.doubleValue()));
            }
        });
        probabilityTable.getColumns().setAll(label, value);
    }

    private void refreshData() {
        experiments.setAll(manager.getAllExperiments().values());
        runs.setAll(manager.getAllRuns().values());
        results.setAll(manager.getAllResults().values());
        updateAnalyzeState();
        setStatus("Данные обновлены");
    }

    private void tryLoadSavedData() {
        if (!localStorage.exists()) {
            return;
        }
        try {
            localStorage.load(manager);
            setStatus("Данные загружены");
        } catch (Exception e) {
            showError("Не удалось загрузить записи", e.getMessage());
        }
    }

    private void saveData() {
        try {
            localStorage.save(manager);
            setStatus("Данные сохранены: " + localStorage.dataFile());
        } catch (Exception e) {
            showError("Не удалось сохранить записи", e.getMessage());
        }
    }

    private void loadData() {
        if (!localStorage.exists()) {
            setStatus("Сохраненные записи не найдены");
            return;
        }
        try {
            localStorage.load(manager);
            refreshData();
            setStatus("Данные загружены: " + localStorage.dataFile());
        } catch (Exception e) {
            showError("Не удалось загрузить записи", e.getMessage());
        }
    }

    private void persistSilently() {
        try {
            localStorage.save(manager);
        } catch (Exception e) {
            setStatus("Ошибка сохранения");
        }
    }
    private void createExperiment() {
        try {
            Experiment experiment = manager.createExperiment(
                    experimentNameField.getText(),
                    experimentDescriptionArea.getText(),
                    authService.getCurrentUsername()
            );
            experimentNameField.clear();
            experimentDescriptionArea.clear();
            refreshData();
            experimentTable.getSelectionModel().select(experiment);
            experimentPicker.getSelectionModel().select(experiment);
            persistSilently();
            setStatus("Эксперимент создан");
        } catch (Exception e) {
            showError("Не удалось создать эксперимент", e.getMessage());
        }
    }

    private void createRun() {
        Experiment experiment = experimentPicker.getValue();
        if (experiment == null) {
            showError("Не выбран эксперимент", "Выберите эксперимент для запуска.");
            return;
        }
        try {
            Run run = manager.createRun(experiment.getId(), runNameField.getText(), authService.getCurrentUsername());
            runNameField.clear();
            refreshData();
            runTable.getSelectionModel().select(run);
            detectionRunBox.getSelectionModel().select(run);
            persistSilently();
            setStatus("Запуск создан");
        } catch (Exception e) {
            showError("Не удалось создать запуск", e.getMessage());
        }
    }

    private void deleteSelectedExperiment() {
        Experiment experiment = experimentTable.getSelectionModel().getSelectedItem();
        if (experiment == null) {
            showError("Не выбран эксперимент", "Выберите строку в таблице.");
            return;
        }
        try {
            manager.removeExperiment(experiment.getId());
            refreshData();
            persistSilently();
            setStatus("Эксперимент удален");
        } catch (Exception e) {
            showError("Не удалось удалить эксперимент", e.getMessage());
        }
    }

    private void deleteSelectedRun() {
        Run run = runTable.getSelectionModel().getSelectedItem();
        if (run == null) {
            showError("Не выбран запуск", "Выберите строку в таблице.");
            return;
        }
        try {
            manager.removeRun(run.getId());
            refreshData();
            persistSilently();
            setStatus("Запуск удален");
        } catch (Exception e) {
            showError("Не удалось удалить запуск", e.getMessage());
        }
    }

    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите изображение");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Изображения", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.webp"));
        File selected = chooser.showOpenDialog(stage);
        if (selected == null) {
            return;
        }
        selectedImage = selected.toPath();
        imagePathField.setText(selectedImage.toString());
        imageView.setImage(new Image(selected.toURI().toString(), true));
        predictionLabel.setText("Результат: -");
        confidenceLabel.setText("Уверенность: -");
        probabilities.clear();
        updateAnalyzeState();
        setStatus("Изображение выбрано");
    }

    private void analyzeSelectedImage() {
        Run run = detectionRunBox.getValue();
        Path image = selectedImage;
        if (run == null || image == null) {
            updateAnalyzeState();
            return;
        }
        String pythonExecutable = pythonField.getText();
        Path projectRoot = Path.of("").toAbsolutePath().normalize();
        Task<SampleDetectionPrediction> task = new Task<>() {
            @Override
            protected SampleDetectionPrediction call() {
                return new SampleDetector(projectRoot, pythonExecutable).predict(image);
            }
        };
        task.setOnRunning(event -> setBusy(true, "Детекция выполняется"));
        task.setOnSucceeded(event -> {
            setBusy(false, "Детекция завершена");
            savePrediction(run, image, task.getValue());
        });
        task.setOnFailed(event -> {
            setBusy(false, "Ошибка детекции");
            Throwable error = task.getException();
            showError("Детекция не выполнена", error == null ? "Неизвестная ошибка" : error.getMessage());
        });
        Thread thread = new Thread(task, "sample-detection-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void savePrediction(Run run, Path image, SampleDetectionPrediction prediction) {
        predictionLabel.setText("Результат: " + prediction.getPredictedClass());
        confidenceLabel.setText(String.format(Locale.ROOT, "Уверенность: %.4f", prediction.getConfidence()));
        probabilities.setAll(prediction.getProbabilities().entrySet().stream()
                .map(entry -> new ProbabilityRow(entry.getKey(), entry.getValue()))
                .toList());
        String comment = "class=" + prediction.getPredictedClass()
                + "; model=" + prediction.getModelName()
                + "; image=" + image.toAbsolutePath().normalize()
                + "; probabilities=" + prediction.formatProbabilities();
        Result result = manager.createResult(
                run.getId(),
                MeasurementParam.SAMPLE_DETECTION_CONFIDENCE,
                prediction.getConfidence(),
                "probability",
                comment
        );
        refreshData();
        resultTable.getSelectionModel().select(result);
        persistSilently();
    }

    private void updateAnalyzeState() {
        if (analyzeButton != null) {
            analyzeButton.setDisable(selectedImage == null || detectionRunBox.getValue() == null);
        }
    }

    private void setBusy(boolean busy, String status) {
        progressIndicator.setVisible(busy);
        analyzeButton.setDisable(busy || selectedImage == null || detectionRunBox.getValue() == null);
        setStatus(status);
    }

    private void choosePythonExecutable() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите python.exe");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Python", "python.exe", "*.exe"));
        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            pythonField.setText(selected.getAbsolutePath());
            setStatus("Python выбран");
        }
    }

    private String defaultPythonExecutable() {
        String configured = firstNonBlank(
                System.getenv("SAMPLE_DETECTION_PYTHON"),
                System.getProperty("sampleDetection.python")
        );
        if (configured != null) {
            return configured;
        }

        Path localVenv = Path.of("").toAbsolutePath().normalize().resolve(".venv").resolve("Scripts").resolve("python.exe");
        if (Files.isRegularFile(localVenv)) {
            return localVenv.toString();
        }
        return "";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String formatDate(Instant instant) {
        return instant == null ? "" : dateFormatter.format(instant);
    }

    private StringConverter<Experiment> experimentConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Experiment experiment) {
                return experiment == null ? "" : experiment.getId() + " - " + experiment.getName();
            }

            @Override
            public Experiment fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<Run> runConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Run run) {
                return run == null ? "" : run.getId() + " - " + run.getName();
            }

            @Override
            public Run fromString(String string) {
                return null;
            }
        };
    }

    private void setStatus(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.setContentText(message == null || message.isBlank() ? "Неизвестная ошибка" : message);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private void applyStyles(Scene scene) {
        String resource = getClass().getResource("/ui/sample-detection.css") == null
                ? null
                : getClass().getResource("/ui/sample-detection.css").toExternalForm();
        if (resource != null) {
            scene.getStylesheets().add(resource);
        }
    }

    public static final class ProbabilityRow {
        private final SimpleStringProperty label;
        private final SimpleDoubleProperty probability;

        public ProbabilityRow(String label, double probability) {
            this.label = new SimpleStringProperty(label);
            this.probability = new SimpleDoubleProperty(probability);
        }

        public SimpleStringProperty labelProperty() {
            return label;
        }

        public SimpleDoubleProperty probabilityProperty() {
            return probability;
        }
    }
}
