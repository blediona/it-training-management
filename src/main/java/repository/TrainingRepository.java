package repository;

import models.Training;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class TrainingRepository {
    public Training create(Training training) {
        String query = """
                INSERT INTO trainings(name, instructor, price, level, description, start_date, end_date, capacity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, training.getName());
            pstm.setString(2, training.getInstructor());
            pstm.setDouble(3, training.getPrice());
            pstm.setString(4, training.getLevel());
            pstm.setString(5, training.getDescription());
            pstm.setDate(6, training.getStartDate() == null ? null : Date.valueOf(training.getStartDate()));
            pstm.setDate(7, training.getEndDate() == null ? null : Date.valueOf(training.getEndDate()));
            pstm.setInt(8, training.getCapacity());
            pstm.executeUpdate();
            return training;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ArrayList<Training> getAll() {
        String query = "SELECT * FROM trainings ORDER BY name";
        ArrayList<Training> trainings = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query);
                ResultSet res = pstm.executeQuery()
        ) {
            while (res.next()) {
                trainings.add(Training.getFromResultSet(res));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return trainings;
    }
}
