package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.dto.LoginRequestDto;
import models.dto.LoginResponseDto;
import services.AuthService;
import services.I18nService;
import services.SceneService;

public class LoginController {
    private static String preservedUsername = "";
    private static String preservedPassword = "";
    private static String preservedMessage = "";
    private final AuthService authService = new AuthService();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Label messageLabel;
    @FXML private MenuButton languageMenuButton;

    @FXML
    private void initialize() {
        usernameField.setText(preservedUsername);
        passwordField.setText(preservedPassword);
        visiblePasswordField.setText(preservedPassword);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        messageLabel.setText(preservedMessage);
        applyLanguageButtonStyles();
    }

    @FXML
    private void handleTogglePasswordVisibility() {
        togglePasswordVisibility(passwordField, visiblePasswordField);
    }

    @FXML
    private void handleLogin() {
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();

        LoginRequestDto loginDto = new LoginRequestDto(username, password);
        LoginResponseDto response = this.authService.login(loginDto);

        if (response.isLogin()) {
            clearPreservedLoginState();
            SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
            return;
        }

        this.messageLabel.setText(response.getMessage());
        preservedMessage = response.getMessage();
    }

    @FXML
    private void handleForgotPassword() {
        clearPreservedLoginState();
        SceneService.loadView("reset-password-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleAlbanianLanguage() {
        changeLanguage("sq");
    }

    @FXML
    private void handleEnglishLanguage() {
        changeLanguage("en");
    }

    @FXML
    private void handleGermanLanguage() {
        changeLanguage("de");
    }

    private void changeLanguage(String languageCode) {
        preservedUsername = usernameField.getText();
        preservedPassword = passwordField.getText();
        preservedMessage = messageLabel.getText();
        I18nService.setLanguage(languageCode);
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    private void applyLanguageButtonStyles() {
        languageMenuButton.setText(I18nService.getCurrentLanguageLabel());
    }

    private void clearPreservedLoginState() {
        preservedUsername = "";
        preservedPassword = "";
        preservedMessage = "";
    }

    private void togglePasswordVisibility(PasswordField hiddenField, TextField visibleField) {
        boolean showPassword = !visibleField.isVisible();
        visibleField.setVisible(showPassword);
        visibleField.setManaged(showPassword);
        hiddenField.setVisible(!showPassword);
        hiddenField.setManaged(!showPassword);
    }
}
