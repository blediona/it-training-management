package services;

import models.Payment;
import models.Candidate;
import repository.CandidateRepository;
import repository.PaymentRepository;

import java.time.LocalDate;
import java.util.ArrayList;

public class PaymentService {
    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final CandidateRepository candidateRepository = new CandidateRepository();

    public Payment save(int candidateId, double amount, LocalDate paymentDate, String method) {
        Candidate candidate = candidateRepository.getById(candidateId);
        if (candidate == null) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.missing"));
        }
        if (amount <= 0) {
            throw new IllegalArgumentException(I18nService.get("validation.payment.negative"));
        }
        if (paymentDate == null || method == null || method.isBlank()) {
            throw new IllegalArgumentException(I18nService.get("validation.required"));
        }
        if (amount > candidate.getBalance()) {
            throw new IllegalArgumentException(I18nService.get("validation.payment.tooHigh"));
        }

        Payment payment = paymentRepository.create(new Payment(0, candidateId, amount, paymentDate, method.trim()));
        if (payment == null) {
            throw new IllegalArgumentException(I18nService.get("validation.save.failed"));
        }
        candidateRepository.addPaymentAmount(candidateId, amount);
        return payment;
    }

    public ArrayList<Payment> getAll() {
        return paymentRepository.getAll();
    }
}
