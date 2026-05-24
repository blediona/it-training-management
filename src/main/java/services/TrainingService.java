package services;

import models.Training;
import repository.TrainingRepository;

import java.time.LocalDate;
import java.util.ArrayList;

public class TrainingService {
    private static final String NAME_PATTERN = "^[A-Za-zÇËçë ]+$";
    private final TrainingRepository trainingRepository = new TrainingRepository();

    public Training save(String name, String instructor, double price) {
        return save(name, instructor, price, "Beginner", "", null, null, 20);
    }

    public Training save(String name, String instructor, double price, String level, String description,
                         LocalDate startDate, LocalDate endDate, int capacity) {
        if (name == null || name.isBlank() || instructor == null || instructor.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("validation.required"));
        }
        if (!instructor.trim().matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("validation.name.letters"));
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
        Training training = trainingRepository.create(new Training(
                0,
                name.trim(),
                instructor.trim(),
                price,
                level == null || level.isBlank() ? "Beginner" : level.trim(),
                description == null ? "" : description.trim(),
                startDate,
                endDate,
                capacity
        ));
        if (training == null) {
            throw new IllegalArgumentException(I18nService.get("validation.save.failed"));
        }
        return training;
    }

    public ArrayList<Training> getAll() {
        return trainingRepository.getAll();
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
}
