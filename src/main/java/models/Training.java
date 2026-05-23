package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Training {
    private int id;
    private String name;
    private String instructor;
    private double price;
    private String level;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int capacity;

    public Training(int id, String name, String instructor, double price) {
        this(id, name, instructor, price, "Beginner", "", null, null, 20);
    }

    public Training(int id, String name, String instructor, double price, String level, String description,
                    LocalDate startDate, LocalDate endDate, int capacity) {
        this.id = id;
        this.name = name;
        this.instructor = instructor;
        this.price = price;
        this.level = level;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
    }

    public static Training getFromResultSet(ResultSet res) throws SQLException {
        return new Training(
                res.getInt("id"),
                res.getString("name"),
                res.getString("instructor"),
                res.getDouble("price"),
                res.getString("level"),
                res.getString("description"),
                res.getDate("start_date") == null ? null : res.getDate("start_date").toLocalDate(),
                res.getDate("end_date") == null ? null : res.getDate("end_date").toLocalDate(),
                res.getInt("capacity")
        );
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
}