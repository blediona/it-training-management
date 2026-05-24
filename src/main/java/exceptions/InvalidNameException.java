package exceptions;

import services.I18nService;

public class InvalidNameException extends Exception {
    public InvalidNameException(String name) {
        super(I18nService.get("validation.name.required"));
    }
}