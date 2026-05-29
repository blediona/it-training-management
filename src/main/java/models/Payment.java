package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Payment {
    private int id;
    private int candidateId;
    private double amount;
    private LocalDate paymentDate;
    private String method;

    public Payment(int id, int candidateId, double amount, LocalDate paymentDate, String method) {
        this.id = id;
        this.candidateId = candidateId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
    }

    public static Payment getFromResultSet(ResultSet res) throws SQLException {
        return new Payment(
                res.getInt("id"),
                res.getInt("candidate_id"),
                res.getDouble("amount"),
                parseDatabaseDate(res.getString("payment_date")),
                res.getString("method")
        );
    }

    private static LocalDate parseDatabaseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            long timestamp = Long.parseLong(value);
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }

    public int getId() { return id; }
    public int getCandidateId() { return candidateId; }
    public double getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getMethod() { return method; }
}
