package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Training {
    private int id;
    private String name;
    private String instructor;
    private String trainerEmail;
    private double price;
    private String level;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int capacity;
    private int trainerUserId;

    public Training(int id, String name, String instructor, double price) {
        this(id, name, instructor, "", price, "Beginner", "", null, null, 20, 0);
    }

    public Training(int id, String name, String instructor, double price, String level, String description,
                    LocalDate startDate, LocalDate endDate, int capacity) {
        this(id, name, instructor, "", price, level, description, startDate, endDate, capacity, 0);
    }

    public Training(int id, String name, String instructor, double price, String level, String description,
                    LocalDate startDate, LocalDate endDate, int capacity, int trainerUserId) {
        this(id, name, instructor, "", price, level, description, startDate, endDate, capacity, trainerUserId);
    }

    public Training(int id, String name, String instructor, String trainerEmail, double price, String level, String description,
                    LocalDate startDate, LocalDate endDate, int capacity, int trainerUserId) {
        this.id = id;
        this.name = name;
        this.instructor = instructor;
        this.trainerEmail = trainerEmail;
        this.price = price;
        this.level = level;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.trainerUserId = trainerUserId;
    }

    public static Training getFromResultSet(ResultSet res) throws SQLException {
        return new Training(
                res.getInt("id"),
                res.getString("name"),
                res.getString("instructor"),
                readOptionalString(res, "trainer_email"),
                res.getDouble("price"),
                res.getString("level"),
                res.getString("description"),
                res.getDate("start_date") == null ? null : res.getDate("start_date").toLocalDate(),
                res.getDate("end_date") == null ? null : res.getDate("end_date").toLocalDate(),
                res.getInt("capacity"),
                res.getInt("trainer_user_id")
        );
    }

    private static String readOptionalString(ResultSet res, String column) throws SQLException {
        try {
            String value = res.getString(column);
            return value == null ? "" : value;
        } catch (SQLException e) {
            return "";
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getTrainerEmail() {
        return trainerEmail;
    }

    public double getPrice() {
        return price;
    }

    public String getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getTrainerUserId() {
        return trainerUserId;
    }
}