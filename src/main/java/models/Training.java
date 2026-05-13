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