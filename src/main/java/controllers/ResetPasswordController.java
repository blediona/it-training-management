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
    @FXML private TextField visibleNewPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        visibleNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        visibleNewPasswordField.setVisible(false);
        visibleNewPasswordField.setManaged(false);
        visibleConfirmPasswordField.setVisible(false);
        visibleConfirmPasswordField.setManaged(false);
    }

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
    private void handleToggleNewPasswordVisibility() {
        togglePasswordVisibility(newPasswordField, visibleNewPasswordField);
    }

    @FXML
    private void handleToggleConfirmPasswordVisibility() {
        togglePasswordVisibility(confirmPasswordField, visibleConfirmPasswordField);
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

    private void togglePasswordVisibility(PasswordField hiddenField, TextField visibleField) {
        boolean showPassword = !visibleField.isVisible();
        visibleField.setVisible(showPassword);
        visibleField.setManaged(showPassword);
        hiddenField.setVisible(!showPassword);
        hiddenField.setManaged(!showPassword);
    }
}
