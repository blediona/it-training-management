package models;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Candidate {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private int trainingId;
    private String trainingName;
    private CandidateStatus status;
    private double totalFee;
    private double paidAmount;
    private LocalDate registrationDate;
    private boolean certificateIssued;

    public Candidate(int id, String firstName, String lastName,String email, String phone, int trainingId,
                     String trainingName, CandidateStatus status, double totalFee, double paidAmount,
                     LocalDate registrationDate, boolean certificateIssued){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.trainingId = trainingId;
        this.trainingName = trainingName;
        this.status = status;
        this.totalFee = totalFee;
        this.paidAmount = paidAmount;
        this.registrationDate = registrationDate;
        this.certificateIssued = certificateIssued;
    }

    public static Candidate getFromResultSet(ResultSet res) throws SQLException{
        return new Candidate(
                res.getInt("id"),
                res.getString("first_name"),
                res.getString("last_name"),
                res.getString("email"),
                res.getString("phone"),
                res.getInt("training_id"),
                res.getString("training_name"),
                CandidateStatus.valueOf(res.getString("status")),
                res.getDouble("total_fee"),
                res.getDouble("paid_amount"),
                parseDatabaseDate(res.getString("registration_date")),
                res.getInt("certificate_issued") == 1
        );
    }

    private static LocalDate parseDatabaseDate(String value){
        if(value==null || value.isBlank()){
            return LocalDate.now();
        }
        try{
            return LocalDate.parse(value);
        } catch (Exception ignored){
            long timestamp = Long.parseLong(value);
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public int getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(int trainingId) {
        this.trainingId = trainingId;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getBalance() {
        return totalFee - paidAmount;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isCertificateIssued() {
        return certificateIssued;
    }

    public void setCertificateIssued(boolean certificateIssued) {
        this.certificateIssued = certificateIssued;
    }


}
