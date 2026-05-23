package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Certificate {
    private int id;
    private int candidateId;
    private String certificateNumber;
    private LocalDate issuedDate;
    private String notes;

    public Certificate(int id, int candidateId, String certificateNumber, LocalDate issuedDate, String notes) {
        this.id = id;
        this.candidateId = candidateId;
        this.certificateNumber = certificateNumber;
        this.issuedDate = issuedDate;
        this.notes = notes;
    }

    public static Certificate getFromResultSet(ResultSet res) throws SQLException {
        return new Certificate(
                res.getInt("id"),
                res.getInt("candidate_id"),
                res.getString("certificate_number"),
                parseDatabaseDate(res.getString("issued_date")),
                res.getString("notes")
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

    public int getId() {
        return id;
    }
    public int getCandidateId() {
        return candidateId;
    }
    public String getCertificateNumber() {
        return certificateNumber;
    }
    public LocalDate getIssuedDate() {
        return issuedDate;
    }
    public String getNotes() {
        return notes == null ? "" : notes;
    }
}
