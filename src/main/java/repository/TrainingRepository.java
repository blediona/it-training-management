package repository;

import models.Training;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;

public class TrainingRepository {
    public Training create(Training training) {
        String query = """
                INSERT INTO trainings(name, instructor, trainer_email, price, level, description, start_date, end_date, capacity, trainer_user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, training.getName());
            pstm.setString(2, training.getInstructor());
            pstm.setString(3, training.getTrainerEmail());
            pstm.setDouble(4, training.getPrice());
            pstm.setString(5, training.getLevel());
            pstm.setString(6, training.getDescription());
            pstm.setDate(7, training.getStartDate() == null ? null : Date.valueOf(training.getStartDate()));
            pstm.setDate(8, training.getEndDate() == null ? null : Date.valueOf(training.getEndDate()));
            pstm.setInt(9, training.getCapacity());
            if (training.getTrainerUserId() > 0) {
                pstm.setInt(10, training.getTrainerUserId());
            } else {
                pstm.setNull(10, Types.INTEGER);
            }
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

    public Training getById(int id) {
        String query = "SELECT * FROM trainings WHERE id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, id);
            try (ResultSet res = pstm.executeQuery()) {
                if (res.next()) {
                    return Training.getFromResultSet(res);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean updateTrainerAccount(int trainingId, int trainerUserId, String trainerEmail) {
        String query = """
                UPDATE trainings
                SET trainer_user_id = ?, trainer_email = ?
                WHERE id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, trainerUserId);
            pstm.setString(2, trainerEmail);
            pstm.setInt(3, trainingId);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public ArrayList<Training> getAllByTrainerUserId(int trainerUserId) {
        String query = "SELECT * FROM trainings WHERE trainer_user_id = ? ORDER BY name";
        ArrayList<Training> trainings = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, trainerUserId);
            try (ResultSet res = pstm.executeQuery()) {
                while (res.next()) {
                    trainings.add(Training.getFromResultSet(res));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return trainings;
    }

    public boolean existsByName(String name) {
        String query = "SELECT COUNT(*) FROM trainings WHERE LOWER(name) = LOWER(?)";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, name == null ? "" : name.trim());
            try (ResultSet res = pstm.executeQuery()) {
                return res.next() && res.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean delete(int trainingId) {
        try (Connection connection = DatabaseService.getConnection()) {
            if (connection == null) {
                return false;
            }
            connection.setAutoCommit(false);
            try {
                deleteCertificatesForTraining(connection, trainingId);
                deletePaymentsForTraining(connection, trainingId);
                deleteCandidatesForTraining(connection, trainingId);

                try (PreparedStatement pstm = connection.prepareStatement("DELETE FROM trainings WHERE id = ?")) {
                    pstm.setInt(1, trainingId);
                    boolean deleted = pstm.executeUpdate() > 0;
                    connection.commit();
                    return deleted;
                }
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public boolean hasCandidates(int trainingId) {
        String query = "SELECT COUNT(*) FROM candidates WHERE training_id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, trainingId);
            try (ResultSet res = pstm.executeQuery()) {
                return res.next() && res.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean allCandidatesCertified(int trainingId) {
        String query = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN status = 'CERTIFIED' AND certificate_issued = 1 THEN 1 ELSE 0 END) AS certified
                FROM candidates
                WHERE training_id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, trainingId);
            try (ResultSet res = pstm.executeQuery()) {
                if (res.next()) {
                    int total = res.getInt("total");
                    int certified = res.getInt("certified");
                    return total > 0 && total == certified;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private void deleteCertificatesForTraining(Connection connection, int trainingId) throws Exception {
        String query = """
                DELETE cert
                FROM certificates cert
                INNER JOIN candidates c ON c.id = cert.candidate_id
                WHERE c.training_id = ?
                """;
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, trainingId);
            pstm.executeUpdate();
        }
    }

    private void deletePaymentsForTraining(Connection connection, int trainingId) throws Exception {
        String query = """
                DELETE p
                FROM payments p
                INNER JOIN candidates c ON c.id = p.candidate_id
                WHERE c.training_id = ?
                """;
        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, trainingId);
            pstm.executeUpdate();
        }
    }

    private void deleteCandidatesForTraining(Connection connection, int trainingId) throws Exception {
        try (PreparedStatement pstm = connection.prepareStatement("DELETE FROM candidates WHERE training_id = ?")) {
            pstm.setInt(1, trainingId);
            pstm.executeUpdate();
        }
    }

    public Integer findIdByName(String name) {
        String query = "SELECT id FROM trainings WHERE LOWER(name) = LOWER(?)";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, name == null ? "" : name.trim());
            try (ResultSet res = pstm.executeQuery()) {
                if (res.next()) {
                    return res.getInt("id");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Double findPriceByName(String name) {
        String query = "SELECT price FROM trainings WHERE LOWER(name) = LOWER(?)";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, name == null ? "" : name.trim());
            try (ResultSet res = pstm.executeQuery()) {
                if (res.next()) {
                    return res.getDouble("price");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String findInstructorByCandidateId(int candidateId) {
        String query = """
                SELECT t.instructor
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE c.id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, candidateId);
            try (ResultSet res = pstm.executeQuery()) {
                if (res.next()) {
                    return res.getString("instructor");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}