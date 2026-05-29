package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.I18nService;
import services.PasswordResetService;
import services.SceneService;

public class ResetPasswordController {
    private final PasswordResetService passwordResetService = new PasswordResetService();

    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleSendResetCode() {
        try {
            passwordResetService.requestReset(emailField.getText());
            setMessage(I18nService.get("auth.resetCodeSent"), true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            setMessage(e.getMessage(), false);
        }
    }

    @FXML
    private void handleChangePassword() {
        try {
            passwordResetService.resetPassword(
                    tokenField.getText(),
                    newPasswordField.getText(),
                    confirmPasswordField.getText()
            );
            emailField.clear();
            tokenField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            setMessage(I18nService.get("auth.resetSuccess"), true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            setMessage(e.getMessage(), false);
        }
    }

    @FXML
    private void handleBackToLogin() {
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    private void setMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("success-text", "error-text");
        messageLabel.getStyleClass().add(success ? "success-text" : "error-text");
    }
}
