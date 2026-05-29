package repository;

import models.Candidate;
import models.dto.DashboardStats;
import models.mappers.CandidateMapper;
import models.mappers.Mapper;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CandidateRepository extends BaseRepository<Candidate> {
    @Override
    String getInsertQuery() {
        return """
                INSERT INTO candidates(first_name, last_name, email, phone, training_id, status,
                                       total_fee, paid_amount, registration_date, certificate_issued)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    String getUpdateQuery() {
        return """
                UPDATE candidates
                SET first_name = ?, last_name = ?, email = ?, phone = ?, training_id = ?, status = ?,
                    total_fee = ?, paid_amount = ?, registration_date = ?, certificate_issued = ?
                WHERE id = ?
                """;
    }

    @Override
    Mapper<Candidate> getMapper() {
        return new CandidateMapper();
    }

    @Override
    String tableName() {
        return "candidates";
    }

    @Override
    void setPstmCreate(PreparedStatement pstm, Candidate candidate) throws SQLException {
        bindCandidate(pstm, candidate);
    }

    @Override
    void setPstmUpdate(PreparedStatement pstm, Candidate candidate) throws SQLException {
        bindCandidate(pstm, candidate);
        pstm.setInt(11, candidate.getId());
    }

    @Override
    public boolean delete(Candidate obj) {
        return this.delete(obj.getId());
    }

    public ArrayList<Candidate> getAll() {
        String query = """
                SELECT c.*, t.name AS training_name
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                ORDER BY c.id ASC
                """;
        ArrayList<Candidate> candidates = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query);
                ResultSet res = pstm.executeQuery()
        ) {
            while (res.next()) {
                Candidate candidate = getMapper().getFromResultSet(res);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return candidates;
    }

    public ArrayList<Candidate> getAllByTrainerUserId(int trainerUserId) {
        String query = """
                SELECT c.*, t.name AS training_name
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE t.trainer_user_id = ?
                ORDER BY c.id ASC
                """;
        ArrayList<Candidate> candidates = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, trainerUserId);
            try (ResultSet res = pstm.executeQuery()) {
                while (res.next()) {
                    Candidate candidate = getMapper().getFromResultSet(res);
                    if (candidate != null) {
                        candidates.add(candidate);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return candidates;
    }

    public DashboardStats getDashboardStatsByTrainerUserId(int trainerUserId) {
        String query = """
                SELECT COUNT(*) AS total_candidates,
                       SUM(CASE WHEN c.status IN ('REGISTERED','IN_TRAINING') THEN 1 ELSE 0 END) AS active_candidates,
                       SUM(CASE WHEN c.certificate_issued = 1 THEN 1 ELSE 0 END) AS certified_candidates,
                       0 AS total_revenue,
                       0 AS outstanding_balance
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE t.trainer_user_id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, trainerUserId);
            try (ResultSet res = pstm.executeQuery()) {
                if (res.next()) {
                    return new DashboardStats(
                            res.getInt("total_candidates"),
                            res.getInt("active_candidates"),
                            res.getInt("certified_candidates"),
                            res.getDouble("total_revenue"),
                            res.getDouble("outstanding_balance")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new DashboardStats(0, 0, 0, 0, 0);
    }

    public DashboardStats getDashboardStats() {
        String query = """
                SELECT COUNT(*) AS total_candidates,
                       SUM(CASE WHEN status IN ('REGISTERED','IN_TRAINING') THEN 1 ELSE 0 END) AS active_candidates,
                       SUM(CASE WHEN certificate_issued = 1 THEN 1 ELSE 0 END) AS certified_candidates,
                       COALESCE(SUM(paid_amount), 0) AS total_revenue,
                       COALESCE(SUM(total_fee - paid_amount), 0) AS outstanding_balance
                FROM candidates
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query);
                ResultSet res = pstm.executeQuery()
        ) {
            if (res.next()) {
                return new DashboardStats(
                        res.getInt("total_candidates"),
                        res.getInt("active_candidates"),
                        res.getInt("certified_candidates"),
                        res.getDouble("total_revenue"),
                        res.getDouble("outstanding_balance")
                );
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new DashboardStats(0, 0, 0, 0, 0);
    }

    public boolean existsByEmailAndTraining(String email, String trainingName) {
        String query = """
                SELECT COUNT(*)
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE LOWER(c.email) = LOWER(?) AND LOWER(t.name) = LOWER(?)
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, email == null ? "" : email.trim());
            pstm.setString(2, trainingName == null ? "" : trainingName.trim());
            try (ResultSet resultSet = pstm.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public ArrayList<Candidate> getReadyForCertification() {
        String query = """
                SELECT c.*, t.name AS training_name
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE c.status = 'COMPLETED' AND c.certificate_issued = 0
                ORDER BY c.id ASC
                """;
        ArrayList<Candidate> candidates = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query);
                ResultSet res = pstm.executeQuery()
        ) {
            while (res.next()) {
                Candidate candidate = getMapper().getFromResultSet(res);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return candidates;
    }

    public ArrayList<Candidate> getReadyForCertificationByTrainerUserId(int trainerUserId) {
        String query = """
                SELECT c.*, t.name AS training_name
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE c.status = 'COMPLETED'
                  AND c.certificate_issued = 0
                  AND t.trainer_user_id = ?
                ORDER BY c.id ASC
                """;
        ArrayList<Candidate> candidates = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, trainerUserId);
            try (ResultSet res = pstm.executeQuery()) {
                while (res.next()) {
                    Candidate candidate = getMapper().getFromResultSet(res);
                    if (candidate != null) {
                        candidates.add(candidate);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return candidates;
    }

    @Override
    public Candidate getById(int id) {
        String query = """
                SELECT c.*, t.name AS training_name
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE c.id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, id);
            try (ResultSet res = pstm.executeQuery()) {
                if (res.next()) {
                    return getMapper().getFromResultSet(res);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
        return null;
    }

    public boolean addPaymentAmount(int candidateId, double amount) {
        String query = "UPDATE candidates SET paid_amount = paid_amount + ? WHERE id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setDouble(1, amount);
            pstm.setInt(2, candidateId);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean markCertificateIssued(int candidateId) {
        String query = "UPDATE candidates SET certificate_issued = 1, status = 'CERTIFIED' WHERE id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, candidateId);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean belongsToTrainer(int candidateId, int trainerUserId) {
        String query = """
                SELECT COUNT(*)
                FROM candidates c
                INNER JOIN trainings t ON t.id = c.training_id
                WHERE c.id = ? AND t.trainer_user_id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, candidateId);
            pstm.setInt(2, trainerUserId);
            try (ResultSet res = pstm.executeQuery()) {
                return res.next() && res.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int candidateId, String status) {
        String query = "UPDATE candidates SET status = ? WHERE id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, status);
            pstm.setInt(2, candidateId);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private void bindCandidate(PreparedStatement pstm, Candidate candidate) throws SQLException {
        pstm.setString(1, candidate.getFirstName());
        pstm.setString(2, candidate.getLastName());
        pstm.setString(3, candidate.getEmail());
        pstm.setString(4, candidate.getPhone());
        pstm.setInt(5, candidate.getTrainingId());
        pstm.setString(6, candidate.getStatus().name());
        pstm.setDouble(7, candidate.getTotalFee());
        pstm.setDouble(8, candidate.getPaidAmount());
        pstm.setString(9, candidate.getRegistrationDate().toString());
        pstm.setInt(10, candidate.isCertificateIssued() ? 1 : 0);
    }
}
