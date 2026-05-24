package controllers;

import exceptions.InvalidNameException;
import exceptions.InvalidPaymentException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import models.Candidate;
import models.CandidateStatus;
import models.dto.CandidateRequestDto;
import models.dto.DashboardStats;
import services.CandidateService;
import services.I18nService;
import services.SceneService;
import services.SessionService;
import services.TrainingService;

import java.util.Locale;
import java.util.Optional;

public class MainController {
    private static final String NAME_INPUT_PATTERN = "[A-Za-zÇËçë ]*";
    private static final String MONEY_INPUT_PATTERN = "\\d*([.,]\\d*)?";
    private final CandidateService candidateService = new CandidateService();
    private final TrainingService trainingService = new TrainingService();
    private final ObservableList<Candidate> candidates = FXCollections.observableArrayList();
    private Integer selectedCandidateId = null;

    @FXML private BorderPane rootPane;
    @FXML private VBox candidateFormPane;
    @FXML private TabPane contentTabPane;
    @FXML private Tab dashboardTab;
    @FXML private TableView<Candidate> candidatesTable;
    @FXML private TableColumn<Candidate, Integer> idColumn;
    @FXML private TableColumn<Candidate, String> nameColumn;
    @FXML private TableColumn<Candidate, String> emailColumn;
    @FXML private TableColumn<Candidate, String> trainingColumn;
    @FXML private TableColumn<Candidate, CandidateStatus> statusColumn;
    @FXML private TableColumn<Candidate, Double> paidColumn;
    @FXML private TableColumn<Candidate, Double> balanceColumn;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> trainingComboBox;
    @FXML private TextField totalFeeField;
    @FXML private TextField paidAmountField;
    @FXML private TextField searchField;
    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label trainingErrorLabel;
    @FXML private Label totalFeeErrorLabel;
    @FXML private Label paidAmountErrorLabel;
    @FXML private ChoiceBox<String> statusChoice;
    @FXML private CheckBox certificateCheckBox;
    @FXML private RadioButton cashRadio;
    @FXML private RadioButton bankRadio;
    @FXML private RadioButton cardRadio;
    @FXML private Button trainingsButton;
    @FXML private Button paymentsButton;
    @FXML private Button certificatesButton;
    @FXML private MenuButton languageMenuButton;
    @FXML private Label statusLabel;
    @FXML private Label totalCandidatesLabel;
    @FXML private Label activeCandidatesLabel;
    @FXML private Label certifiedCandidatesLabel;
    @FXML private Label revenueLabel;
    @FXML private Label balanceLabel;
    @FXML private Label aiRecommendationLabel;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> financeBarChart;

    @FXML
    private void initialize() {
        prepareTable();
        prepareForm();
        prepareAccessibility();
        prepareTabOrder();
        prepareShortcuts();
        prepareRoleAccess();
        setSidebarVisible(candidateFormPane, false);
        loadCandidates();
    }

    @FXML
    private void handleNewCandidate() {
        setSidebarVisible(candidateFormPane, true);
        clearForm();
        statusLabel.setText(I18nService.get("status.newCandidate"));
    }

    @FXML
    private void handleToggleCandidateForm() {
        setSidebarVisible(candidateFormPane, !candidateFormPane.isVisible());
    }

    @FXML
    private void handleSaveCandidate() {
        try {
            if (!validateCandidateForm()) {
                showWarning(I18nService.get("validation.form.invalid"));
                return;
            }
            CandidateRequestDto request = createRequestDto();
            if (selectedCandidateId == null) {
                candidateService.save(request);
            } else {
                candidateService.update(request, selectedCandidateId);
            }
            loadCandidates();
            clearForm();
            statusLabel.setText(I18nService.get("status.saved"));
        } catch (InvalidNameException | InvalidPaymentException e) {
            showWarning(e.getMessage());
        } catch (NumberFormatException e) {
            showWarning(I18nService.get("validation.payment.number"));
        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        } catch (IllegalStateException e) {
            showWarning(I18nService.get("validation.database.error") + "\n" + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCandidate() {
        if (!ensureAdminAccess()) {
            return;
        }
        Candidate selected = candidatesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(I18nService.get("validation.selection.required"));
            return;
        }
        if (!confirmAction(
                I18nService.get("confirm.delete.title"),
                String.format(I18nService.get("confirm.delete.candidate"), selected.getFullName())
        )) {
            statusLabel.setText(I18nService.get("status.deleteCancelled"));
            return;
        }
        candidateService.delete(selected.getId());
        loadCandidates();
        clearForm();
        statusLabel.setText(I18nService.get("status.deleted"));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            candidatesTable.setItems(candidates);
            return;
        }

        candidatesTable.setItems(candidates.filtered(candidate ->
                candidate.getFullName().toLowerCase(Locale.ROOT).contains(query)
                        || candidate.getEmail().toLowerCase(Locale.ROOT).contains(query)
                        || candidate.getTrainingName().toLowerCase(Locale.ROOT).contains(query)));
    }

    @FXML
    private void handleRefresh() {
        loadCandidates();
        statusLabel.setText(I18nService.get("status.refreshed"));
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleLogout() {
        if (!confirmAction(I18nService.get("confirm.logout.title"), I18nService.get("confirm.logout"))) {
            statusLabel.setText(I18nService.get("status.logoutCancelled"));
            return;
        }

        SessionService.clear();
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleOpenDashboard() {
        loadCandidates();
        contentTabPane.getSelectionModel().select(dashboardTab);
    }

    private boolean confirmAction(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18nService.get("dialog.confirmation"));
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void handleOpenTrainings() {
        SceneService.loadView("trainings-view.fxml", I18nService.get("training.title"));
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
    private void handleOpenHelpPage() {
        SceneService.loadView("help-view.fxml", I18nService.get("help.title"));
    }

    @FXML
    private void handleAlbanianLanguage() {
        I18nService.setLanguage("sq");
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleEnglishLanguage() {
        I18nService.setLanguage("en");
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleGermanLanguage() {
        I18nService.setLanguage("de");
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleShowHelp() {
        handleOpenHelpPage();
    }

    private void prepareTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        trainingColumn.setCellValueFactory(new PropertyValueFactory<>("trainingName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paidColumn.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        MenuItem editItem = new MenuItem(I18nService.get("context.edit"));
        editItem.setOnAction(event -> fillForm(candidatesTable.getSelectionModel().getSelectedItem()));
        MenuItem deleteItem = new MenuItem(I18nService.get("context.delete"));
        deleteItem.setOnAction(event -> handleDeleteCandidate());
        if (SessionService.isAdmin()) {
            candidatesTable.setContextMenu(new ContextMenu(editItem, deleteItem));
        } else {
            candidatesTable.setContextMenu(new ContextMenu(editItem));
        }

        candidatesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> fillForm(selected));
    }

    private void prepareForm() {
        statusChoice.setItems(FXCollections.observableArrayList("REGISTERED", "IN_TRAINING", "COMPLETED", "CERTIFIED"));
        statusChoice.setValue("REGISTERED");
        trainingComboBox.setItems(FXCollections.observableArrayList(
                trainingService.getAll().stream().map(training -> training.getName()).toList()
        ));

        installNameFormatter(firstNameField, firstNameErrorLabel);
        installNameFormatter(lastNameField, lastNameErrorLabel);
        installMoneyFormatter(totalFeeField, totalFeeErrorLabel);
        installMoneyFormatter(paidAmountField, paidAmountErrorLabel);

        ToggleGroup paymentGroup = new ToggleGroup();
        cashRadio.setToggleGroup(paymentGroup);
        bankRadio.setToggleGroup(paymentGroup);
        cardRadio.setToggleGroup(paymentGroup);
        cashRadio.setSelected(true);
    }

    private void prepareAccessibility() {
        firstNameField.setAccessibleText(I18nService.get("candidate.firstName"));
        lastNameField.setAccessibleText(I18nService.get("candidate.lastName"));
        emailField.setAccessibleText(I18nService.get("candidate.email"));
        phoneField.setAccessibleText(I18nService.get("candidate.phone"));
        trainingComboBox.setAccessibleText(I18nService.get("candidate.training"));
        statusChoice.setAccessibleText(I18nService.get("candidate.status"));
        totalFeeField.setAccessibleText(I18nService.get("candidate.totalFee"));
        paidAmountField.setAccessibleText(I18nService.get("candidate.paidAmount"));
        searchField.setAccessibleText(I18nService.get("search.prompt"));
        candidatesTable.setAccessibleText(I18nService.get("accessibility.candidatesTable"));
        statusLabel.setAccessibleText(I18nService.get("accessibility.statusBar"));

        firstNameField.setTooltip(new Tooltip(I18nService.get("candidate.firstName")));
        lastNameField.setTooltip(new Tooltip(I18nService.get("candidate.lastName")));
        emailField.setTooltip(new Tooltip(I18nService.get("candidate.email")));
        phoneField.setTooltip(new Tooltip(I18nService.get("candidate.phone")));
        trainingComboBox.setTooltip(new Tooltip(I18nService.get("candidate.training")));
        totalFeeField.setTooltip(new Tooltip(I18nService.get("candidate.totalFee")));
        paidAmountField.setTooltip(new Tooltip(I18nService.get("candidate.paidAmount")));
        searchField.setTooltip(new Tooltip(I18nService.get("search.prompt")));
        candidatesTable.setTooltip(new Tooltip(I18nService.get("accessibility.tableShortcut")));

        firstNameField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateFirstNameField();
            }
        });
        lastNameField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateLastNameField();
            }
        });
        emailField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateEmailField();
            }
        });
        phoneField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                validatePhoneField();
            }
        });
        trainingComboBox.valueProperty().addListener((observable, oldValue, value) -> {
            if (validateTrainingField()) {
                fillTotalFeeFromTraining();
            }
        });
        trainingComboBox.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                if (validateTrainingField()) {
                    fillTotalFeeFromTraining();
                }
            }
        });
        totalFeeField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateTotalFeeField();
            }
        });
        paidAmountField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                validatePaidAmountField();
            }
        });
    }

    private void prepareRoleAccess() {
        boolean admin = SessionService.isAdmin();
        paymentsButton.setDisable(!admin);
        certificatesButton.setDisable(!admin);
        certificateCheckBox.setDisable(!admin);
        paidAmountField.setDisable(!admin);
        cashRadio.setDisable(!admin);
        bankRadio.setDisable(!admin);
        cardRadio.setDisable(!admin);

        paymentsButton.setTooltip(new Tooltip(admin ? I18nService.get("nav.payments") : I18nService.get("access.denied.staff")));
        certificatesButton.setTooltip(new Tooltip(admin ? I18nService.get("nav.certificates") : I18nService.get("access.denied.staff")));
        certificateCheckBox.setTooltip(new Tooltip(admin ? I18nService.get("candidate.certificateIssued") : I18nService.get("access.denied.staff")));
        paidAmountField.setTooltip(new Tooltip(admin ? I18nService.get("candidate.paidAmount") : I18nService.get("access.denied.staff")));

        if (!admin && (paidAmountField.getText() == null || paidAmountField.getText().isBlank())) {
            paidAmountField.setText("0");
        }

        applyLanguageButtonStyles();
        statusLabel.setText(String.format(I18nService.get("status.loggedRole"), SessionService.getCurrentRole()));
    }

    private boolean ensureAdminAccess() {
        if (SessionService.isAdmin()) {
            return true;
        }
        showWarning(I18nService.get("access.denied.staff"));
        statusLabel.setText(I18nService.get("access.denied.staff"));
        return false;
    }

    private void prepareTabOrder() {
        Node[] focusOrder = {
                firstNameField,
                lastNameField,
                emailField,
                phoneField,
                trainingComboBox,
                statusChoice,
                totalFeeField,
                paidAmountField,
                cashRadio,
                bankRadio,
                cardRadio,
                certificateCheckBox,
                candidatesTable,
                searchField
        };

        for (int i = 0; i < focusOrder.length; i++) {
            int index = i;
            focusOrder[i].addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.TAB) {
                    int direction = event.isShiftDown() ? -1 : 1;
                    int nextIndex = (index + direction + focusOrder.length) % focusOrder.length;
                    focusOrder[nextIndex].requestFocus();
                    event.consume();
                }
            });
        }
    }

    private void prepareShortcuts() {
        rootPane.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene == null) {
                return;
            }
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::handleNewCandidate);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::handleSaveCandidate);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), this::handleDeleteCandidate);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), this::handleRefresh);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::handleShowHelp);
        });
    }

    private void loadCandidates() {
        var loadedCandidates = candidateService.getAll();
        if (loadedCandidates == null) {
            showWarning(I18nService.get("validation.load.failed"));
            return;
        }

        candidates.setAll(loadedCandidates);
        candidatesTable.setItems(candidates);
        updateDashboard();
    }

    private CandidateRequestDto createRequestDto() {
        return new CandidateRequestDto(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                trainingComboBox.getValue(),
                statusChoice.getValue(),
                parseMoney(totalFeeField.getText()),
                getPaidAmountForRequest(),
                getCertificateIssuedForRequest()
        );
    }

    private double getPaidAmountForRequest() {
        if (SessionService.isAdmin()) {
            return parseMoney(paidAmountField.getText());
        }

        Candidate selected = selectedCandidateId == null ? null : candidatesTable.getSelectionModel().getSelectedItem();
        return selected == null ? 0 : selected.getPaidAmount();
    }

    private boolean getCertificateIssuedForRequest() {
        if (SessionService.isAdmin()) {
            return certificateCheckBox.isSelected();
        }

        Candidate selected = selectedCandidateId == null ? null : candidatesTable.getSelectionModel().getSelectedItem();
        return selected != null && selected.isCertificateIssued();
    }

    private boolean validateCandidateForm() {
        boolean validFirstName = validateFirstNameField();
        boolean validLastName = validateLastNameField();
        boolean validEmail = validateEmailField();
        boolean validPhone = validatePhoneField();
        boolean validTraining = validateTrainingField();
        boolean validTotalFee = validateTotalFeeField();
        boolean validPaidAmount = validatePaidAmountField();
        return validFirstName && validLastName && validEmail && validPhone && validTraining && validTotalFee && validPaidAmount;
    }

    private boolean validateFirstNameField() {
        return validateNameField(firstNameErrorLabel, firstNameField);
    }

    private boolean validateLastNameField() {
        return validateNameField(lastNameErrorLabel, lastNameField);
    }

    private boolean validateNameField(Label errorLabel, TextField field) {
        String value = field.getText() == null ? "" : field.getText().trim();
        boolean valid = !value.isEmpty() && value.matches(NAME_INPUT_PATTERN);
        setFieldError(errorLabel, field, valid ? "" : I18nService.get("validation.name.letters"));
        return valid;
    }

    private boolean validateEmailField() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        boolean valid = email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        setFieldError(emailErrorLabel, emailField, valid ? "" : I18nService.get("validation.email.invalid"));
        return valid;
    }

    private boolean validatePhoneField() {
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        boolean valid = phone.matches("^\\+383\\d{8}$");
        setFieldError(phoneErrorLabel, phoneField, valid ? "" : I18nService.get("validation.phone.invalid"));
        return valid;
    }

    private boolean validateTrainingField() {
        String training = trainingComboBox.getValue() == null ? "" : trainingComboBox.getValue().trim();
        boolean valid = !training.isEmpty() && trainingService.existsByName(training);
        setFieldError(trainingErrorLabel, trainingComboBox, valid ? "" : I18nService.get("validation.training.notRegistered"));
        return valid;
    }

    private void fillTotalFeeFromTraining() {
        Double price = trainingService.findPriceByName(trainingComboBox.getValue());
        if (price == null) {
            return;
        }

        totalFeeField.setText(String.format(Locale.US, "%.2f", price));
        setFieldError(totalFeeErrorLabel, totalFeeField, "");
    }

    private boolean validateTotalFeeField() {
        return validateMoneyField(totalFeeErrorLabel, totalFeeField);
    }

    private boolean validatePaidAmountField() {
        if (!SessionService.isAdmin()) {
            setFieldError(paidAmountErrorLabel, paidAmountField, "");
            return true;
        }
        return validateMoneyField(paidAmountErrorLabel, paidAmountField);
    }

    private boolean validateMoneyField(Label errorLabel, TextField field) {
        try {
            double value = parseMoney(field.getText());
            boolean valid = value >= 0;
            setFieldError(errorLabel, field, valid ? "" : I18nService.get("validation.payment.negative.short"));
            return valid;
        } catch (Exception e) {
            setFieldError(errorLabel, field, I18nService.get("validation.number.short"));
            return false;
        }
    }

    private void setFieldError(Label errorLabel, Control field, String message) {
        boolean hasError = message != null && !message.isBlank();
        errorLabel.setText(message);
        errorLabel.setVisible(hasError);
        errorLabel.setManaged(hasError);
        field.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("error"), hasError);
    }

    private void fillForm(Candidate candidate) {
        if (candidate == null) {
            return;
        }
        setSidebarVisible(candidateFormPane, true);
        selectedCandidateId = candidate.getId();
        firstNameField.setText(candidate.getFirstName());
        lastNameField.setText(candidate.getLastName());
        emailField.setText(candidate.getEmail());
        phoneField.setText(candidate.getPhone());
        trainingComboBox.setValue(candidate.getTrainingName());
        statusChoice.setValue(candidate.getStatus().name());
        totalFeeField.setText(String.valueOf(candidate.getTotalFee()));
        paidAmountField.setText(String.valueOf(candidate.getPaidAmount()));
        certificateCheckBox.setSelected(candidate.isCertificateIssued());
    }

    private void clearForm() {
        selectedCandidateId = null;
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        trainingComboBox.setValue(null);
        statusChoice.setValue("REGISTERED");
        totalFeeField.clear();
        paidAmountField.setText(SessionService.isAdmin() ? "" : "0");
        certificateCheckBox.setSelected(false);
        setFieldError(emailErrorLabel, emailField, "");
        setFieldError(firstNameErrorLabel, firstNameField, "");
        setFieldError(lastNameErrorLabel, lastNameField, "");
        setFieldError(phoneErrorLabel, phoneField, "");
        setFieldError(trainingErrorLabel, trainingComboBox, "");
        setFieldError(totalFeeErrorLabel, totalFeeField, "");
        setFieldError(paidAmountErrorLabel, paidAmountField, "");
        candidatesTable.getSelectionModel().clearSelection();
    }

    private void updateDashboard() {
        DashboardStats stats = candidateService.getDashboardStats();
        totalCandidatesLabel.setText(String.valueOf(stats.getTotalCandidates()));
        activeCandidatesLabel.setText(String.valueOf(stats.getActiveCandidates()));
        certifiedCandidatesLabel.setText(String.valueOf(stats.getCertifiedCandidates()));
        revenueLabel.setText(String.format(Locale.US, "%.2f EUR", stats.getTotalRevenue()));
        balanceLabel.setText(String.format(Locale.US, "%.2f EUR", stats.getOutstandingBalance()));
        aiRecommendationLabel.setText(buildAiRecommendation(stats));

        int registered = (int) candidates.stream().filter(c -> c.getStatus() == CandidateStatus.REGISTERED).count();
        int training = (int) candidates.stream().filter(c -> c.getStatus() == CandidateStatus.IN_TRAINING).count();
        int completed = (int) candidates.stream().filter(c -> c.getStatus() == CandidateStatus.COMPLETED).count();
        int certified = (int) candidates.stream().filter(c -> c.getStatus() == CandidateStatus.CERTIFIED).count();

        statusPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data(I18nService.get("status.registered"), registered),
                new PieChart.Data(I18nService.get("status.inTraining"), training),
                new PieChart.Data(I18nService.get("status.completed"), completed),
                new PieChart.Data(I18nService.get("status.certified"), certified)
        ));

        XYChart.Series<String, Number> financeSeries = new XYChart.Series<>();
        financeSeries.setName(I18nService.get("dashboard.finance"));
        financeSeries.getData().add(new XYChart.Data<>(I18nService.get("dashboard.revenue"), stats.getTotalRevenue()));
        financeSeries.getData().add(new XYChart.Data<>(I18nService.get("dashboard.balance"), stats.getOutstandingBalance()));
        financeBarChart.getData().setAll(financeSeries);
    }

    private String buildAiRecommendation(DashboardStats stats) {
        long candidatesWithDebt = candidates.stream()
                .filter(candidate -> candidate.getBalance() > 0)
                .count();
        long completedWithoutCertificate = candidates.stream()
                .filter(candidate -> candidate.getStatus() == CandidateStatus.COMPLETED && !candidate.isCertificateIssued())
                .count();
        Candidate highestDebtCandidate = candidates.stream()
                .filter(candidate -> candidate.getBalance() > 0)
                .max((first, second) -> Double.compare(first.getBalance(), second.getBalance()))
                .orElse(null);

        if (stats.getTotalCandidates() == 0) {
            return I18nService.get("dashboard.ai.empty");
        }

        if (highestDebtCandidate == null && completedWithoutCertificate == 0) {
            return I18nService.get("dashboard.ai.clear");
        }

        String highestDebtText = highestDebtCandidate == null
                ? I18nService.get("dashboard.ai.noDebt")
                : String.format(
                Locale.US,
                I18nService.get("dashboard.ai.highestDebt"),
                highestDebtCandidate.getFullName(),
                highestDebtCandidate.getBalance()
        );

        return String.format(
                Locale.US,
                I18nService.get("dashboard.ai.dynamic"),
                candidatesWithDebt,
                stats.getOutstandingBalance(),
                completedWithoutCertificate,
                highestDebtText
        );
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(I18nService.get("dialog.warning"));
        alert.setHeaderText(I18nService.get("dialog.checkData"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setSidebarVisible(VBox sidebar, boolean visible) {
        sidebar.setVisible(visible);
        sidebar.setManaged(visible);
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

    private void installMoneyFormatter(TextField field, Label errorLabel) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches(MONEY_INPUT_PATTERN)) {
                if (!change.getControlNewText().isBlank()) {
                    setFieldError(errorLabel, field, "");
                }
                return change;
            }

            setFieldError(errorLabel, field, I18nService.get("validation.number.short"));
            return null;
        }));
    }

    private double parseMoney(String value) {
        return Double.parseDouble(value.replace(',', '.'));
    }

    private void applyLanguageButtonStyles() {
        languageMenuButton.setText(I18nService.getCurrentLanguageLabel());
    }
}
