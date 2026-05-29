package repository;

import models.User;
import models.mappers.Mapper;
import models.mappers.UserMapper;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserRepository extends BaseRepository<User> {
    @Override
    String getInsertQuery() {
        return """
                INSERT INTO users(full_name, email, username, salt, salted_hash, role_id)
                VALUES (?, ?, ?, ?, ?, (SELECT id FROM roles WHERE name = ?))
                """;
    }

    @Override
    String getUpdateQuery() {
        return """
                UPDATE users
                SET full_name = ?, email = ?, username = ?, salt = ?, salted_hash = ?,
                    role_id = (SELECT id FROM roles WHERE name = ?)
                WHERE id = ?
                """;
    }

    @Override
    Mapper<User> getMapper() {
        return new UserMapper();
    }

    @Override
    String tableName() {
        return "users";
    }

    @Override
    void setPstmCreate(PreparedStatement pstm, User obj) throws SQLException {
        pstm.setString(1, obj.getFullName());
        pstm.setString(2, obj.getEmail());
        pstm.setString(3, obj.getUsername());
        pstm.setString(4, obj.getSalt());
        pstm.setString(5, obj.getSaltedHash());
        pstm.setString(6, obj.getRole());
    }

    @Override
    void setPstmUpdate(PreparedStatement pstm, User obj) throws SQLException {
        setPstmCreate(pstm, obj);
        pstm.setInt(7, obj.getId());
    }

    @Override
    public boolean delete(User obj) {
        return this.delete(obj.getId());
    }

    public User getByUsername(String username) {
        String query = """
                SELECT u.*, r.name AS role
                FROM users u
                INNER JOIN roles r ON r.id = u.role_id
                WHERE u.username = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, username);
            ResultSet res = pstm.executeQuery();
            if (res.next()) {
                return getMapper().getFromResultSet(res);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    public User getByEmail(String email) {
        String query = """
                SELECT u.*, r.name AS role
                FROM users u
                INNER JOIN roles r ON r.id = u.role_id
                WHERE LOWER(u.email) = LOWER(?)
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, email == null ? "" : email.trim());
            ResultSet res = pstm.executeQuery();
            if (res.next()) {
                return getMapper().getFromResultSet(res);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean existsByEmail(String email) {
        return getByEmail(email) != null;
    }

    public boolean updatePassword(int userId, String salt, String saltedHash) {
        String query = """
                UPDATE users
                SET salt = ?, salted_hash = ?
                WHERE id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, salt);
            pstm.setString(2, saltedHash);
            pstm.setInt(3, userId);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public boolean updateRole(int userId, String roleName) {
        String query = """
                UPDATE users
                SET role_id = (SELECT id FROM roles WHERE name = ?)
                WHERE id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, roleName);
            pstm.setInt(2, userId);
            return pstm.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public ArrayList<User> getByRole(String roleName) {
        String query = """
                SELECT u.*, r.name AS role
                FROM users u
                INNER JOIN roles r ON r.id = u.role_id
                WHERE r.name = ?
                ORDER BY u.full_name
                """;
        ArrayList<User> users = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, roleName);
            ResultSet res = pstm.executeQuery();
            while (res.next()) {
                users.add(getMapper().getFromResultSet(res));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return users;
    }

    @Override
    public User getById(int id) {
        String query = """
                SELECT u.*, r.name AS role
                FROM users u
                INNER JOIN roles r ON r.id = u.role_id
                WHERE u.id = ?
                """;
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, id);
            ResultSet res = pstm.executeQuery();
            if (res.next()) {
                return getMapper().getFromResultSet(res);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
        return null;
    }
}
