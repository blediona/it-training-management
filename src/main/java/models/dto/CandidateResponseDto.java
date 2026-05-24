package models.dto;

public class CandidateResponseDto {
    private int id;
    private String fullName;
    private String email;
    private String trainingName;
    private String status;
    private double paidAmount;
    private double balance;

    public CandidateResponseDto(int id, String fullName,String email, String trainingName,
                                String status, double paidAmount, double balance){
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.trainingName = trainingName;
        this.status = status;
        this.paidAmount = paidAmount;
        this.balance = balance;
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

    public String getTrainingName() {
        return trainingName;
    }

    public String getStatus() {
        return status;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public double getBalance() {
        return balance;
    }
}
