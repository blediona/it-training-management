package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Training;
import services.I18nService;
import services.SceneService;
import services.SessionService;
import services.TrainingService;

import java.util.Locale;
import java.util.Optional;

public class TrainingController {
    private static final String NAME_INPUT_PATTERN = "[A-Za-zÇËçë ]*";
    private static final String INTEGER_INPUT_PATTERN = "\\d*";
    private static final String MONEY_INPUT_PATTERN = "\\d*([.,]\\d*)?";
    private final TrainingService trainingService = new TrainingService();
    private final ObservableList<Training> trainings = FXCollections.observableArrayList();

    @FXML private TextField nameField;
    @FXML private VBox trainingFormPane;
    @FXML private TextField searchField;
    @FXML private TextField instructorField;
    @FXML private Label instructorErrorLabel;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> levelComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField capacityField;
    @FXML private Label activeProgramsLabel;
    @FXML private Label instructorCountLabel;
    @FXML private Label averagePriceLabel;
    @FXML private Button paymentsButton;
    @FXML private Button certificatesButton;
    @FXML private MenuButton languageMenuButton;
    @FXML private TableView<Training> trainingsTable;
    @FXML private TableColumn<Training, Integer> idColumn;
    @FXML private TableColumn<Training, String> nameColumn;
    @FXML private TableColumn<Training, String> instructorColumn;
    @FXML private TableColumn<Training, Double> priceColumn;
    @FXML private TableColumn<Training, String> levelColumn;
    @FXML private TableColumn<Training, Integer> capacityColumn;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        levelComboBox.setItems(FXCollections.observableArrayList("Beginner", "Intermediate", "Advanced"));
        levelComboBox.setValue("Beginner");
        capacityField.setText("20");
        installNameFormatter(instructorField, instructorErrorLabel);
        installMoneyFormatter(priceField);
        installIntegerFormatter(capacityField);
        instructorField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateInstructorField();
            }
        });
        setSidebarVisible(trainingFormPane, false);
        prepareRoleAccess();
        applyLanguageButtonStyles();
        loadTrainings();
    }

    @FXML
    private void handleSaveTraining() {
        try {
            if (!validateInstructorField()) {
                showWarning(I18nService.get("validation.form.invalid"));
                return;
            }
            double price = parseMoney(priceField.getText());
            int capacity = Integer.parseInt(capacityField.getText());
            trainingService.save(
                    nameField.getText(),
                    instructorField.getText(),
                    price,
                    levelComboBox.getValue(),
                    descriptionArea.getText(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue(),
                    capacity
            );
            clearForm();
            loadTrainings();
        } catch (NumberFormatException e) {
            showWarning(I18nService.get("validation.number"));
        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        }
    }

    private void clearForm() {
        nameField.clear();
        instructorField.clear();
        priceField.clear();
        descriptionArea.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        levelComboBox.setValue("Beginner");
        capacityField.setText("20");
        setFieldError(instructorErrorLabel, instructorField, "");
    }

    @FXML
    private void handleBack() {
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleToggleTrainingForm() {
        setSidebarVisible(trainingFormPane, !trainingFormPane.isVisible());
    }

    @FXML
    private void handleOpenDashboard() {
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleShowHelp() {
        SceneService.loadView("help-view.fxml", I18nService.get("help.title"));
    }

    @FXML
    private void handleOpenTrainings() {
        loadTrainings();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            trainingsTable.setItems(trainings);
            return;
        }

        trainingsTable.setItems(trainings.filtered(training ->
                training.getName().toLowerCase(Locale.ROOT).contains(query)
                        || training.getInstructor().toLowerCase(Locale.ROOT).contains(query)
                        || training.getLevel().toLowerCase(Locale.ROOT).contains(query)
        ));
    }

    @FXML
    private void handleOpenPayments() {
        if (!ensureAdminAccess()) {
            return;
        }
        SceneService.loadView("payments-view.fxml", I18nService.get("payment.title"));
    }

    @FXML
    private void handleOpenCertificates() {
        if (!ensureAdminAccess()) {
            return;
        }
        SceneService.loadView("certificates-view.fxml", I18nService.get("certificate.title"));
    }

    @FXML
    private void handleLogout() {
        if (!confirmAction(I18nService.get("confirm.logout.title"), I18nService.get("confirm.logout"))) {
            return;
        }
        SessionService.clear();
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    private void loadTrainings() {
        trainings.setAll(trainingService.getAll());
        trainingsTable.setItems(trainings);
        activeProgramsLabel.setText(String.valueOf(trainings.size()));
        instructorCountLabel.setText(String.valueOf(trainings.stream().map(Training::getInstructor).distinct().count()));
        double average = trainings.stream().mapToDouble(Training::getPrice).average().orElse(0);
        averagePriceLabel.setText(String.format(Locale.US, "%.2f EUR", average));
    }

    @FXML
    private void handleAlbanianLanguage() {
        I18nService.setLanguage("sq");
        SceneService.loadView("trainings-view.fxml", I18nService.get("training.title"));
    }

    @FXML
    private void handleEnglishLanguage() {
        I18nService.setLanguage("en");
        SceneService.loadView("trainings-view.fxml", I18nService.get("training.title"));
    }

    @FXML
    private void handleGermanLanguage() {
        I18nService.setLanguage("de");
        SceneService.loadView("trainings-view.fxml", I18nService.get("training.title"));
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(I18nService.get("dialog.warning"));
        alert.setHeaderText(I18nService.get("dialog.checkData"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean ensureAdminAccess() {
        if (SessionService.isAdmin()) {
            return true;
        }
        showWarning(I18nService.get("access.denied.staff"));
        return false;
    }

    private void prepareRoleAccess() {
        boolean admin = SessionService.isAdmin();
        paymentsButton.setDisable(!admin);
        certificatesButton.setDisable(!admin);
        paymentsButton.setTooltip(new javafx.scene.control.Tooltip(admin ? I18nService.get("nav.payments") : I18nService.get("access.denied.staff")));
        certificatesButton.setTooltip(new javafx.scene.control.Tooltip(admin ? I18nService.get("nav.certificates") : I18nService.get("access.denied.staff")));
    }

    private void applyLanguageButtonStyles() {
        languageMenuButton.setText(I18nService.getCurrentLanguageLabel());
    }

    private boolean confirmAction(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18nService.get("dialog.confirmation"));
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void setSidebarVisible(VBox sidebar, boolean visible) {
        sidebar.setVisible(visible);
        sidebar.setManaged(visible);
    }

    private boolean validateInstructorField() {
        String value = instructorField.getText() == null ? "" : instructorField.getText().trim();
        boolean valid = !value.isEmpty() && value.matches(NAME_INPUT_PATTERN);
        setFieldError(instructorErrorLabel, instructorField, valid ? "" : I18nService.get("validation.name.letters"));
        return valid;
    }

    private void setFieldError(Label errorLabel, TextField field, String message) {
        boolean hasError = message != null && !message.isBlank();
        errorLabel.setText(message);
        errorLabel.setVisible(hasError);
        errorLabel.setManaged(hasError);
        field.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("error"), hasError);
    }

    private void installNameFormatter(TextField field, Label errorLabel) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches(NAME_INPUT_PATTERN)) {
                if (!change.getControlNewText().isBlank()) {
                    setFieldError(errorLabel, field, "");
                }
                return change;
            }

            setFieldError(errorLabel, field, I18nService.get("validation.name.letters"));
            return null;
        }));
    }

    private void installMoneyFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(MONEY_INPUT_PATTERN) ? change : null
        ));
    }

    private void installIntegerFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(INTEGER_INPUT_PATTERN) ? change : null
        ));
    }

    private double parseMoney(String value) {
        return Double.parseDouble(value.replace(',', '.'));
    }
}