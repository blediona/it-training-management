package services;

import models.Training;
import models.User;
import repository.TrainingRepository;
import repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

public class TrainingService {
    private static final String NAME_PATTERN = "^[A-Za-zÃ‡Ã‹Ã§Ã« ]+$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private final TrainingRepository trainingRepository = new TrainingRepository();
    private final UserRepository userRepository = new UserRepository();

    public Training save(String name, String instructor, double price) {
        return save(name, instructor, buildTrainerUsername(instructor) + "@itcenter.local", price, "Beginner", "", null, null, 20);
    }

    public Training save(String name, String instructor, double price, String level, String description,
                         LocalDate startDate, LocalDate endDate, int capacity) {
        return save(name, instructor, buildTrainerUsername(instructor) + "@itcenter.local", price, level, description, startDate, endDate, capacity);
    }

    public Training save(String name, String instructor, String trainerEmail, double price, String level, String description,
                         LocalDate startDate, LocalDate endDate, int capacity) {
        if (name == null || name.isBlank() || instructor == null || instructor.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("validation.required"));
        }
        if (!instructor.trim().matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("validation.name.letters"));
        }
        if (trainerEmail == null || !trainerEmail.trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("validation.email.invalid"));
        }
        if (price < 0) {
            throw new IllegalArgumentException(I18nService.get("validation.payment.negative"));
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException(I18nService.get("validation.number"));
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(I18nService.get("validation.date"));
        }

        int trainerUserId = resolveExistingTrainerUserId(instructor.trim(), trainerEmail.trim());
        Training training = trainingRepository.create(new Training(
                0,
                name.trim(),
                instructor.trim(),
                trainerEmail.trim(),
                price,
                level == null || level.isBlank() ? "Beginner" : level.trim(),
                description == null ? "" : description.trim(),
                startDate,
                endDate,
                capacity,
                trainerUserId
        ));
        if (training == null) {
            throw new IllegalArgumentException(I18nService.get("validation.save.failed"));
        }
        return training;
    }

    private int resolveExistingTrainerUserId(String fullName, String email) {
        String username = buildTrainerUsername(fullName);
        User existingByUsername = userRepository.getByUsername(username);
        if (existingByUsername != null) {
            if ("TRAINER".equalsIgnoreCase(existingByUsername.getRole())) {
                return existingByUsername.getId();
            }
            throw new IllegalArgumentException(I18nService.get("auth.usernameExists"));
        }
        User existingByEmail = userRepository.getByEmail(email);
        if (existingByEmail != null) {
            if ("TRAINER".equalsIgnoreCase(existingByEmail.getRole())) {
                return existingByEmail.getId();
            }
            throw new IllegalArgumentException(I18nService.get("auth.usernameExists"));
        }
        return 0;
    }

    public TrainerAccountResult createTrainerAccount(int trainingId) {
        if (!SessionService.isAdmin()) {
            throw new IllegalArgumentException(I18nService.get("access.denied.staff"));
        }
        Training training = trainingRepository.getById(trainingId);
        if (training == null) {
            throw new IllegalArgumentException(I18nService.get("training.selectToCreateTrainerAccount"));
        }
        if (training.getTrainerUserId() > 0) {
            User linkedUser = userRepository.getById(training.getTrainerUserId());
            throw new IllegalArgumentException(String.format(
                    I18nService.get("training.trainerAccountExists"),
                    linkedUser == null ? training.getInstructor() : linkedUser.getUsername()
            ));
        }
        if (training.getTrainerEmail() == null || !training.getTrainerEmail().trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("training.trainerEmailMissing"));
        }

        String username = buildTrainerUsername(training.getInstructor());
        boolean alreadyExists = userRepository.getByUsername(username) != null || userRepository.getByEmail(training.getTrainerEmail()) != null;
        User trainer = findOrCreateTrainer(training.getInstructor().trim(), training.getTrainerEmail().trim());
        if (trainer == null || trainer.getId() <= 0) {
            throw new IllegalArgumentException(I18nService.get("validation.save.failed"));
        }
        if (!trainingRepository.updateTrainerAccount(trainingId, trainer.getId(), trainer.getEmail())) {
            throw new IllegalArgumentException(I18nService.get("validation.save.failed"));
        }
        return new TrainerAccountResult(trainer, !alreadyExists, buildInitialPassword(training.getInstructor()));
    }

    private User findOrCreateTrainer(String fullName, String email) {
        String username = buildTrainerUsername(fullName);
        User existingByUsername = userRepository.getByUsername(username);
        if (existingByUsername != null) {
            return ensureTrainerRole(existingByUsername);
        }
        User existingByEmail = userRepository.getByEmail(email);
        if (existingByEmail != null) {
            return ensureTrainerRole(existingByEmail);
        }

        String salt = HashService.generateSalt();
        String saltedPassword = HashService.generateHash(buildInitialPassword(fullName), salt);
        User trainer = new User(0, fullName, email, username, salt, saltedPassword, "TRAINER");
        return userRepository.create(trainer);
    }

    private User ensureTrainerRole(User user) {
        if ("TRAINER".equalsIgnoreCase(user.getRole())) {
            return user;
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(I18nService.get("auth.usernameExists"));
        }
        userRepository.updateRole(user.getId(), "TRAINER");
        return userRepository.getById(user.getId());
    }

    private String buildTrainerUsername(String fullName) {
        String username = fullName.trim().toLowerCase(Locale.ROOT)
                .replace("Ã«", "e")
                .replace("Ã§", "c")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("(^_|_$)", "");
        return username.isBlank() ? "trainer" : username;
    }

    private String buildInitialPassword(String fullName) {
        String firstName = fullName.trim().split("\\s+")[0].toLowerCase(Locale.ROOT)
                .replace("ÃƒÂ«", "e")
                .replace("ÃƒÂ§", "c")
                .replace("ë", "e")
                .replace("ç", "c")
                .replaceAll("[^a-z0-9]", "");
        return (firstName.isBlank() ? "trainer" : firstName) + "123";
    }

    public static class TrainerAccountResult {
        private final User user;
        private final boolean created;
        private final String initialPassword;

        public TrainerAccountResult(User user, boolean created, String initialPassword) {
            this.user = user;
            this.created = created;
            this.initialPassword = initialPassword;
        }

        public User getUser() {
            return user;
        }

        public boolean isCreated() {
            return created;
        }

        public String getInitialPassword() {
            return initialPassword;
        }
    }

    public ArrayList<Training> getAll() {
        return trainingRepository.getAll();
    }

    public ArrayList<Training> getAllByTrainerUserId(int trainerUserId) {
        return trainingRepository.getAllByTrainerUserId(trainerUserId);
    }

    public ArrayList<Training> getTrainingsWithoutTrainerAccount() {
        ArrayList<Training> missingAccounts = new ArrayList<>();
        for (Training training : trainingRepository.getAll()) {
            if (training.getTrainerUserId() <= 0) {
                missingAccounts.add(training);
            }
        }
        return missingAccounts;
    }

    public void delete(int trainingId) {
        if (SessionService.isTrainer()) {
            throw new IllegalArgumentException(I18nService.get("access.denied.trainer"));
        }
        if (trainingRepository.hasCandidates(trainingId) && !trainingRepository.allCandidatesCertified(trainingId)) {
            throw new IllegalArgumentException(I18nService.get("training.deleteHasActiveCandidates"));
        }
        try {
            if (!trainingRepository.delete(trainingId)) {
                throw new IllegalArgumentException(I18nService.get("training.deleteFailed"));
            }
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase(Locale.ROOT).contains("foreign key")) {
                throw new IllegalArgumentException(I18nService.get("training.deleteHasActiveCandidates"), e);
            }
            throw e;
        }
    }

    public boolean existsByName(String name) {
        return trainingRepository.existsByName(name);
    }

    public Integer findIdByName(String name) {
        return trainingRepository.findIdByName(name);
    }

    public Double findPriceByName(String name) {
        return trainingRepository.findPriceByName(name);
    }

    public String findInstructorByCandidateId(int candidateId) {
        return trainingRepository.findInstructorByCandidateId(candidateId);
    }
}