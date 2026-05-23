package services;

import models.User;
import repository.PasswordResetRepository;
import repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class PasswordResetService {
    private final UserRepository userRepository = new UserRepository();
    private final PasswordResetRepository passwordResetRepository = new PasswordResetRepository();
    private final SecureRandom secureRandom = new SecureRandom();

    public boolean requestReset(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("validation.email.invalid"));
        }

        User user = userRepository.getByEmail(email);
        if (user == null) {
            return false;
        }

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return passwordResetRepository.createToken(user, token, LocalDateTime.now().plusHours(2));
    }
}
