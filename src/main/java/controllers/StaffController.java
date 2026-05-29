package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import models.User;
import services.I18nService;
import services.SceneService;
import services.SessionService;
import services.StaffService;

import java.util.Optional;

public class StaffController {
    private static final String NAME_INPUT_PATTERN = "[A-Za-zÃƒâ€¡Ãƒâ€¹ÃƒÂ§ÃƒÂ« ]*";
    private final StaffService staffService = new StaffService();
    private final ObservableList<User> staffUsers = FXCollections.observableArrayList();

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Label fullNameErrorLabel;
    @FXML private Label messageLabel;
    @FXML private MenuButton languageMenuButton;
    @FXML private TableView<User> staffTable;
    @FXML private TableColumn<User, Integer> staffIdColumn;
    @FXML private TableColumn<User, String> staffNameColumn;
    @FXML private TableColumn<User, String> staffEmailColumn;
    @FXML private TableColumn<User, String> staffUsernameColumn;

    @FXML
    private void initialize() {
        if (!SessionService.isAdmin()) {
            showWarning(I18nService.get("access.denied.staff"));
            SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
            return;
        }
        prepareTable();
        applyLanguageButtonStyles();
        fullNameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches(NAME_INPUT_PATTERN)) {
                setFieldError("");
                return change;
            }
            setFieldError(I18nService.get("validation.name.letters"));
            return null;
        }));
        loadStaff();
    }

    @FXML
    private void handleRegisterStaff() {
        try {
            User user = staffService.registerStaff(fullNameField.getText(), emailField.getText());
            String password = staffService.buildInitialPassword(fullNameField.getText());
            messageLabel.getStyleClass().removeAll("error-text", "success-text");
            messageLabel.getStyleClass().add("success-text");
            messageLabel.setText(String.format(I18nService.get("staff.created"), user.getUsername(), password));
            fullNameField.clear();
            emailField.clear();
            loadStaff();
        } catch (IllegalArgumentException | IllegalStateException e) {
            messageLabel.getStyleClass().removeAll("success-text", "error-text");
            messageLabel.getStyleClass().add("error-text");
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleDeleteStaff() {
        User selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(I18nService.get("staff.selectToDelete"));
            return;
        }
        if (!confirmAction(I18nService.get("staff.delete"), String.format(I18nService.get("staff.confirmDelete"), selected.getFullName()))) {
            return;
        }
        try {
            staffService.deleteStaff(selected.getId());
            messageLabel.getStyleClass().removeAll("error-text", "success-text");
            messageLabel.getStyleClass().add("success-text");
            messageLabel.setText(I18nService.get("staff.deleted"));
            loadStaff();
        } catch (IllegalArgumentException | IllegalStateException e) {
            messageLabel.getStyleClass().removeAll("success-text", "error-text");
            messageLabel.getStyleClass().add("error-text");
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleOpenDashboard() {
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleOpenTrainings() {
        SceneService.loadView("trainings-view.fxml", I18nService.get("training.title"));
    }

    @FXML
    private void handleOpenPayments() {
        SceneService.loadView("payments-view.fxml", I18nService.get("payment.title"));
    }

    @FXML
    private void handleOpenCertificates() {
        SceneService.loadView("certificates-view.fxml", I18nService.get("certificate.title"));
    }

    @FXML
    private void handleOpenStaff() {
    }

    @FXML
    private void handleShowHelp() {
        SceneService.loadView("help-view.fxml", I18nService.get("help.title"));
    }

    @FXML
    private void handleAlbanianLanguage() {
        I18nService.setLanguage("sq");
        SceneService.loadView("staff-view.fxml", I18nService.get("staff.title"));
    }

    @FXML
    private void handleEnglishLanguage() {
        I18nService.setLanguage("en");
        SceneService.loadView("staff-view.fxml", I18nService.get("staff.title"));
    }

    @FXML
    private void handleGermanLanguage() {
        I18nService.setLanguage("de");
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

    private void setFieldError(String message) {
        boolean hasError = message != null && !message.isBlank();
        fullNameErrorLabel.setText(message);
        fullNameErrorLabel.setVisible(hasError);
        fullNameErrorLabel.setManaged(hasError);
        fullNameField.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("error"), hasError);
    }

    private void prepareTable() {
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        staffNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        staffEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        staffUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        MenuItem deleteItem = new MenuItem(I18nService.get("staff.delete"));
        deleteItem.setOnAction(event -> handleDeleteStaff());
        staffTable.setContextMenu(new ContextMenu(deleteItem));
    }

    private void loadStaff() {
        staffUsers.setAll(staffService.getStaffUsers());
        staffTable.setItems(staffUsers);
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

    private void applyLanguageButtonStyles() {
        languageMenuButton.setText(I18nService.getCurrentLanguageLabel());
    }
}
