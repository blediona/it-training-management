package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import services.I18nService;
import services.SceneService;
import services.SessionService;

import java.util.Optional;

public class HelpController {
    @FXML private Button paymentsButton;
    @FXML private Button certificatesButton;
    @FXML private MenuButton languageMenuButton;

    @FXML
    private void initialize() {
        prepareRoleAccess();
        applyLanguageButtonStyles();
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
    private void handleShowHelp() {
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
    private void handleAlbanianLanguage() {
        I18nService.setLanguage("sq");
        SceneService.loadView("help-view.fxml", I18nService.get("help.title"));
    }

    @FXML
    private void handleEnglishLanguage() {
        I18nService.setLanguage("en");
        SceneService.loadView("help-view.fxml", I18nService.get("help.title"));
    }

    @FXML
    private void handleGermanLanguage() {
        I18nService.setLanguage("de");
        SceneService.loadView("help-view.fxml", I18nService.get("help.title"));
    }

    @FXML
    private void handleLogout() {
        if (!confirmAction(I18nService.get("confirm.logout.title"), I18nService.get("confirm.logout"))) {
            return;
        }
        SessionService.clear();
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
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
        paymentsButton.setTooltip(new Tooltip(admin ? I18nService.get("nav.payments") : I18nService.get("access.denied.staff")));
        certificatesButton.setTooltip(new Tooltip(admin ? I18nService.get("nav.certificates") : I18nService.get("access.denied.staff")));
    }

    private void applyLanguageButtonStyles() {
        languageMenuButton.setText(I18nService.getCurrentLanguageLabel());
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
}