package models.dto;

public class SignupResponseDto {
    private boolean created;
    private String message;

    public SignupResponseDto(boolean created, String message) {
        this.created = created;
        this.message = message;
    }

    public SignupResponseDto(boolean created) {
        this.created = created;
        this.message = "";
    }

    public boolean isCreated() {
        return created;
    }

    public String getMessage() {
        return message;
    }
}
