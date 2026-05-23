package repository;

import models.User;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
}
