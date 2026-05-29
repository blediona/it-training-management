package exceptions;

import services.I18nService;

public class InvalidPaymentException extends Exception {
    public InvalidPaymentException(double amount) {
        super(I18nService.get("validation.payment.tooHigh"));
    }

    public InvalidPaymentException(String message) {
        super(message);
    }
}
