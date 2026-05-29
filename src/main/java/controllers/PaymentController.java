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
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Candidate;
import models.Payment;
import services.CandidateService;
import services.I18nService;
import services.PaymentService;
import services.SceneService;
import services.SessionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PaymentController {
    private static final String INTEGER_INPUT_PATTERN = "\\d*";
    private static final String MONEY_INPUT_PATTERN = "\\d*([.,]\\d*)?";
    private final PaymentService paymentService = new PaymentService();
    private final CandidateService candidateService = new CandidateService();
    private final ObservableList<Payment> payments = FXCollections.observableArrayList();

    @FXML private TextField candidateIdField;
    @FXML private VBox paymentFormPane;
    @FXML private TextField searchField;
    @FXML private TextField amountField;
    @FXML private DatePicker paymentDatePicker;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label monthPaymentsLabel;
    @FXML private Button staffButton;
    @FXML private MenuButton languageMenuButton;
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Integer> idColumn;
    @FXML private TableColumn<Payment, Integer> candidateIdColumn;
    @FXML private TableColumn<Payment, Double> amountColumn;
    @FXML private TableColumn<Payment, LocalDate> paymentDateColumn;
    @FXML private TableColumn<Payment, String> methodColumn;

    @FXML
    private void initialize() {
        if (!SessionService.isAdmin()) {
            showWarning(I18nService.get("access.denied.staff"));
            SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
            return;
        }
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        candidateIdColumn.setCellValueFactory(new PropertyValueFactory<>("candidateId"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        methodComboBox.setItems(FXCollections.observableArrayList(
                I18nService.get("payment.cash"),
                I18nService.get("payment.bank"),
                I18nService.get("payment.card")
        ));
        methodComboBox.setValue(I18nService.get("payment.cash"));
        paymentDatePicker.setValue(LocalDate.now());
        installIntegerFormatter(candidateIdField);
        installMoneyFormatter(amountField);
        setSidebarVisible(paymentFormPane, false);
        applyLanguageButtonStyles();
        loadPayments();
    }

    @FXML
    private void handleSavePayment() {
        try {
            int candidateId = Integer.parseInt(candidateIdField.getText());
            double amount = parseMoney(amountField.getText());
            paymentService.save(candidateId, amount, paymentDatePicker.getValue(), methodComboBox.getValue());
            candidateIdField.clear();
            amountField.clear();
            paymentDatePicker.setValue(LocalDate.now());
            loadPayments();
        } catch (NumberFormatException e) {
            showWarning(I18nService.get("validation.number"));
        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleTogglePaymentForm() {
        setSidebarVisible(paymentFormPane, !paymentFormPane.isVisible());
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
        if (SessionService.isTrainer()) {
            showWarning(I18nService.get("access.denied.trainer"));
            return;
        }
        SceneService.loadView("trainings-view.fxml", I18nService.get("training.title"));
    }

    @FXML
    private void handleOpenPayments() {
        loadPayments();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            paymentsTable.setItems(payments);
            return;
        }

        paymentsTable.setItems(payments.filtered(payment ->
                String.valueOf(payment.getId()).contains(query)
                        || String.valueOf(payment.getCandidateId()).contains(query)
                        || payment.getMethod().toLowerCase(Locale.ROOT).contains(query)
                        || String.valueOf(payment.getPaymentDate()).contains(query)
        ));
    }

    @FXML
    private void handleOpenCertificates() {
        if (!SessionService.isAdmin()) {
            showWarning(I18nService.get("access.denied.staff"));
            return;
        }
        SceneService.loadView("certificates-view.fxml", I18nService.get("certificate.title"));
    }

    @FXML
    private void handleOpenStaff() {
        if (!SessionService.isAdmin()) {
            showWarning(I18nService.get("access.denied.staff"));
            return;
        }
        SceneService.loadView("staff-view.fxml", I18nService.get("staff.title"));
    }

    @FXML
    private void handleLogout() {
        if (!confirmAction(I18nService.get("confirm.logout.title"), I18nService.get("confirm.logout"))) {
            return;
        }
        SessionService.clear();
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleShowPaidCandidates() {
        List<Candidate> paidCandidates = candidateService.getAll().stream()
                .filter(candidate -> candidate.getPaidAmount() > 0)
                .toList();
        showCandidateDetails(I18nService.get("payment.paidCandidates"), paidCandidates);
    }

    @FXML
    private void handleShowDebtCandidates() {
        List<Candidate> debtCandidates = candidateService.getAll().stream()
                .filter(candidate -> candidate.getBalance() > 0)
                .toList();
        showCandidateDetails(I18nService.get("payment.debtCandidates"), debtCandidates);
    }

    @FXML
    private void handleShowMonthlyPayments() {
        List<Payment> monthlyPayments = paymentService.getAll().stream()
                .filter(payment -> payment.getPaymentDate().getMonth().equals(LocalDate.now().getMonth())
                        && payment.getPaymentDate().getYear() == LocalDate.now().getYear())
                .toList();
        showPaymentDetails(I18nService.get("payment.monthlyPayments"), monthlyPayments);
    }

    private void loadPayments() {
        payments.setAll(paymentService.getAll());
        paymentsTable.setItems(payments);
        double total = payments.stream().mapToDouble(Payment::getAmount).sum();
        long thisMonth = payments.stream()
                .filter(payment -> payment.getPaymentDate().getMonth().equals(LocalDate.now().getMonth())
                        && payment.getPaymentDate().getYear() == LocalDate.now().getYear())
                .count();
        totalRevenueLabel.setText(String.format(Locale.US, "%.2f EUR", total));
        totalBalanceLabel.setText(String.format(Locale.US, "%.2f EUR", candidateService.getDashboardStats().getOutstandingBalance()));
        monthPaymentsLabel.setText(String.valueOf(thisMonth));
    }

    @FXML
    private void handleAlbanianLanguage() {
        I18nService.setLanguage("sq");
        SceneService.loadView("payments-view.fxml", I18nService.get("payment.title"));
    }

    @FXML
    private void handleEnglishLanguage() {
        I18nService.setLanguage("en");
        SceneService.loadView("payments-view.fxml", I18nService.get("payment.title"));
    }

    @FXML
    private void handleGermanLanguage() {
        I18nService.setLanguage("de");
        SceneService.loadView("payments-view.fxml", I18nService.get("payment.title"));
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(I18nService.get("dialog.warning"));
        alert.setHeaderText(I18nService.get("dialog.checkData"));
        alert.setContentText(message);
        alert.showAndWait();
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

    private void showCandidateDetails(String title, List<Candidate> candidates) {
        TableView<Candidate> tableView = new TableView<>();
        tableView.setItems(FXCollections.observableArrayList(candidates));

        TableColumn<Candidate, Integer> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        id.setPrefWidth(70);

        TableColumn<Candidate, String> name = new TableColumn<>(I18nService.get("candidate.fullName"));
        name.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        name.setPrefWidth(200);

        TableColumn<Candidate, String> email = new TableColumn<>(I18nService.get("candidate.email"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        email.setPrefWidth(240);

        TableColumn<Candidate, String> training = new TableColumn<>(I18nService.get("candidate.training"));
        training.setCellValueFactory(new PropertyValueFactory<>("trainingName"));
        training.setPrefWidth(220);

        TableColumn<Candidate, Double> paid = new TableColumn<>(I18nService.get("candidate.paidAmount"));
        paid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        paid.setPrefWidth(120);

        TableColumn<Candidate, Double> balance = new TableColumn<>(I18nService.get("dashboard.balance"));
        balance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balance.setPrefWidth(120);

        tableView.getColumns().setAll(id, name, email, training, paid, balance);
        showDetailsWindow(title, tableView);
    }

    private void showPaymentDetails(String title, List<Payment> payments) {
        TableView<Payment> tableView = new TableView<>();
        tableView.setItems(FXCollections.observableArrayList(payments));

        TableColumn<Payment, Integer> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        id.setPrefWidth(70);

        TableColumn<Payment, Integer> candidateId = new TableColumn<>(I18nService.get("payment.candidateId"));
        candidateId.setCellValueFactory(new PropertyValueFactory<>("candidateId"));
        candidateId.setPrefWidth(140);

        TableColumn<Payment, Double> amount = new TableColumn<>(I18nService.get("payment.amount"));
        amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amount.setPrefWidth(140);

        TableColumn<Payment, LocalDate> date = new TableColumn<>(I18nService.get("payment.date"));
        date.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        date.setPrefWidth(170);

        TableColumn<Payment, String> method = new TableColumn<>(I18nService.get("payment.method"));
        method.setCellValueFactory(new PropertyValueFactory<>("method"));
        method.setPrefWidth(170);

        tableView.getColumns().setAll(id, candidateId, amount, date, method);
        showDetailsWindow(title, tableView);
    }

    private void showDetailsWindow(String title, TableView<?> tableView) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        VBox root = new VBox(14, titleLabel, tableView);
        root.getStyleClass().add("details-window");

        Scene scene = new Scene(root, 980, 560);
        scene.getStylesheets().add(SceneService.class.getResource("/styles/app.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void installIntegerFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(INTEGER_INPUT_PATTERN) ? change : null
        ));
    }

    private void installMoneyFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(MONEY_INPUT_PATTERN) ? change : null
        ));
    }

    private double parseMoney(String value) {
        return Double.parseDouble(value.replace(',', '.'));
    }

    private void applyLanguageButtonStyles() {
        languageMenuButton.setText(I18nService.getCurrentLanguageLabel());
    }
}
