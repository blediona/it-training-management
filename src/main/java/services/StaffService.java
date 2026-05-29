package services;

import models.User;
import repository.UserRepository;

import java.util.ArrayList;
import java.util.Locale;

public class StaffService {
    private static final String NAME_PATTERN = "^[A-Za-zÃ‡Ã‹Ã§Ã« ]+$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private final UserRepository userRepository = new UserRepository();

    public User registerStaff(String fullName, String email) {
        if (!SessionService.isAdmin()) {
            throw new IllegalArgumentException(I18nService.get("access.denied.staff"));
        }
        if (fullName == null || fullName.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("validation.required"));
        }
        if (!fullName.trim().matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("validation.name.letters"));
        }
        if (!email.trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("validation.email.invalid"));
        }

        String username = buildUsername(fullName);
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(I18nService.get("auth.usernameExists"));
        }

        String firstName = fullName.trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
        String salt = HashService.generateSalt();
        String saltedPassword = HashService.generateHash(firstName + "123", salt);
        return userRepository.create(new User(0, fullName.trim(), email.trim(), username, salt, saltedPassword, "STAFF"));
    }

    public String buildUsername(String fullName) {
        String username = fullName.trim().toLowerCase(Locale.ROOT)
                .replace("Ã«", "e")
                .replace("Ã§", "c")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("(^_|_$)", "");
        return username.isBlank() ? "staff" : username;
    }

    public String buildInitialPassword(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        return fullName.trim().split("\\s+")[0].toLowerCase(Locale.ROOT) + "123";
    }

    public ArrayList<User> getStaffUsers() {
        if (!SessionService.isAdmin()) {
            throw new IllegalArgumentException(I18nService.get("access.denied.staff"));
        }
        return userRepository.getByRole("STAFF");
    }

    public void deleteStaff(int userId) {
        if (!SessionService.isAdmin()) {
            throw new IllegalArgumentException(I18nService.get("access.denied.staff"));
        }
        User user = userRepository.getById(userId);
        if (user == null || !"STAFF".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(I18nService.get("staff.deleteOnlyStaff"));
        }
        userRepository.delete(userId);
    }
}
