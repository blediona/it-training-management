package services;

import models.User;
import repository.PasswordResetRepository;
import repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class PasswordResetService {
    private final UserRepository userRepository = new UserRepository();
    private final PasswordResetRepository passwordResetRepository = new PasswordResetRepository();
    private final EmailService emailService = new EmailService();
    private final SecureRandom secureRandom = new SecureRandom();

    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("validation.email.invalid"));
        }

        User user = userRepository.getByEmail(email);
        if (user == null) {
            return;
        }

        String token = generateToken();
        boolean created = passwordResetRepository.createToken(user, token, LocalDateTime.now().plusMinutes(30));
        if (!created) {
            throw new IllegalStateException(I18nService.get("auth.resetCreateFailed"));
        }
        emailService.sendPasswordResetCode(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        validatePasswordResetInput(token, newPassword, confirmPassword);

        Integer userId = passwordResetRepository.findValidUserIdByToken(token.trim());
        if (userId == null) {
            throw new IllegalArgumentException(I18nService.get("auth.resetInvalidToken"));
        }

        String salt = HashService.generateSalt();
        String saltedPassword = HashService.generateHash(newPassword, salt);
        boolean updated = userRepository.updatePassword(userId, salt, saltedPassword);
        boolean markedUsed = passwordResetRepository.markTokenUsed(token.trim());

        if (!updated || !markedUsed) {
            throw new IllegalStateException(I18nService.get("auth.resetUpdateFailed"));
        }
    }

    private String generateToken() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    private void validatePasswordResetInput(String token, String newPassword, String confirmPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("auth.resetTokenRequired"));
        }
        if (newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("validation.required"));
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(I18nService.get("auth.passwordMismatch"));
        }
    }
}
