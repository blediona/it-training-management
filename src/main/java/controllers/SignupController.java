package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import models.dto.SignupRequestDto;
import services.I18nService;
import services.SceneService;
import services.SignupService;

public class SignupController {
    private static final String NAME_INPUT_PATTERN = "[A-Za-zÇËçë ]*";
    private final SignupService signUpService = new SignupService();
    private boolean submitAttempted = false;

    @FXML private VBox signupCard;
    @FXML private TextField fullNameField;
    @FXML private Label fullNameErrorLabel;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        setFieldError(fullNameErrorLabel, fullNameField, "");
        fullNameField.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) {
                scene.getWindow().focusedProperty().addListener((windowObservable, wasFocused, isFocused) -> {
                    if (isFocused) {
                        signupCard.requestFocus();
                    }
                });
            }
        });
        fullNameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches(NAME_INPUT_PATTERN)) {
                if (!change.getControlNewText().isBlank()) {
                    setFieldError(fullNameErrorLabel, fullNameField, "");
                }
                return change;
            }

            setFieldError(fullNameErrorLabel, fullNameField, I18nService.get("validation.name.letters"));
            return null;
        }));
    }

    @FXML
    private void handleSignup() {
        submitAttempted = true;
        if (!validateFullNameField()) {
            this.messageLabel.setText(I18nService.get("validation.form.invalid"));
            return;
        }
        SignupRequestDto request = this.createRequestDto();
        SignupResponseDto response = this.signUpService.save(request);

        if (!response.isCreated()) {
            this.messageLabel.setText(response.getMessage());
            return;
        }

        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    private boolean validateFullNameField() {
        String value = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        boolean valid = !value.isEmpty() && value.matches(NAME_INPUT_PATTERN);
        setFieldError(fullNameErrorLabel, fullNameField, valid || !submitAttempted ? "" : I18nService.get("validation.name.required"));
        return valid;
    }

    private void setFieldError(Label errorLabel, TextField field, String message) {
        boolean hasError = message != null && !message.isBlank();
        errorLabel.setText(message);
        errorLabel.setVisible(hasError);
        errorLabel.setManaged(hasError);
        field.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("error"), hasError);
    }

    private SignupRequestDto createRequestDto() {
        String fullName = this.fullNameField.getText();
        String email = this.emailField.getText();
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();
        String confirmPassword = this.confirmPasswordField.getText();

        return new SignupRequestDto(fullName, email, username, password, confirmPassword);
    }

    @FXML
    private void handleGoToLogin() {
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }
}
