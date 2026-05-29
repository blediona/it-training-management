package repository;

import models.User;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class PasswordResetRepository {
    public boolean createToken(User user, String token, LocalDateTime expiresAt) {
        String query = "INSERT INTO password_reset_tokens(user_id, token, expires_at) VALUES (?, ?, ?)";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, user.getId());
            pstm.setString(2, token);
            pstm.setString(3, expiresAt.toString().replace('T', ' '));
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public Integer findValidUserIdByToken(String token) {
        String query = """
                SELECT user_id
                FROM password_reset_tokens
                WHERE token = ?
                  AND used_at IS NULL
                  AND expires_at > NOW()
                ORDER BY created_at DESC
                LIMIT 1
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, token);
            ResultSet res = pstm.executeQuery();
            if (res.next()) {
                return res.getInt("user_id");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean markTokenUsed(String token) {
        String query = """
                UPDATE password_reset_tokens
                SET used_at = NOW()
                WHERE token = ?
                  AND used_at IS NULL
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, token);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
