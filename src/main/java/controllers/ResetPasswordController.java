package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.I18nService;
import services.PasswordResetService;
import services.SceneService;

public class ResetPasswordController {
    private final PasswordResetService passwordResetService = new PasswordResetService();

    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    @FXML
    private void handleResetPassword() {
        try {
            passwordResetService.requestReset(emailField.getText());
            messageLabel.setText(I18nService.get("auth.resetRegistered") + " " + emailField.getText());
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }
}
