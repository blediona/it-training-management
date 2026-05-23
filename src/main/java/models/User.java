package models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String username;
    private String salt;
    private String saltedHash;
    private int roleId;
    private String role;

    public User(int id, String username, String salt, String saltedHash) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.saltedHash = saltedHash;
    }

    public User(int id, String fullName, String email, String username, String salt, String saltedHash, String role) {
        this(id, fullName, email, username, salt, saltedHash, 0, role);
    }

    public User(int id, String fullName, String email, String username, String salt, String saltedHash, int roleId, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.salt = salt;
        this.saltedHash = saltedHash;
        this.roleId = roleId;
        this.role = role;
    }

    public static User getFromResultSet(ResultSet res) throws SQLException {
        return new User(
                res.getInt("id"),
                res.getString("full_name"),
                res.getString("email"),
                res.getString("username"),
                res.getString("salt"),
                res.getString("salted_hash"),
                res.getInt("role_id"),
                res.getString("role")
        );
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public String getSaltedHash() {
        return saltedHash;
    }

    public String getRole() {
        return role;
    }

    public int getRoleId() {
        return roleId;
    }
}
