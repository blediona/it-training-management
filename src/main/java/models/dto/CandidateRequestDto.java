package models.dto;

public class CandidateRequestDto implements IRequestDto{
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String trainingName;
    private String status;
    private double totalFee;
    private double paidAmount;
    private boolean certificateIssued;

    public CandidateRequestDto(String firstName, String lastName, String email, String phone, String trainingName,
                               String status, double totalFee, double paidAmount, boolean certificateIssued) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.trainingName = trainingName;
        this.status = status;
        this.totalFee = totalFee;
        this.paidAmount = paidAmount;
        this.certificateIssued = certificateIssued;
    }

    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public String getTrainingName() {
        return trainingName;
    }
    public String getStatus() {
        return status;
    }
    public double getTotalFee() {
        return totalFee;
    }
    public double getPaidAmount() {
        return paidAmount;
    }
    public boolean isCertificateIssued() {
        return certificateIssued;
    }
}
