package services;

import models.Certificate;
import models.Candidate;
import models.CandidateStatus;
import repository.CandidateRepository;
import repository.CertificateRepository;

import java.time.LocalDate;
import java.util.ArrayList;

public class CertificateService {
    private final CertificateRepository certificateRepository = new CertificateRepository();
    private final CandidateRepository candidateRepository = new CandidateRepository();

    public Certificate save(int candidateId, String certificateNumber, LocalDate issuedDate, String notes) {
        if (SessionService.isStaff()) {
            throw new IllegalArgumentException(I18nService.get("access.denied.staff"));
        }
        Candidate candidate = candidateRepository.getById(candidateId);
        if (candidate == null) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.missing"));
        }
        if (SessionService.isTrainer() && !candidateRepository.belongsToTrainer(candidateId, SessionService.getCurrentUserId())) {
            throw new IllegalArgumentException(I18nService.get("access.denied.trainer"));
        }
        if (candidate.getStatus() != CandidateStatus.COMPLETED) {
            throw new IllegalArgumentException(I18nService.get("validation.certificate.notReady"));
        }
        if (certificateNumber == null || certificateNumber.isBlank() || issuedDate == null) {
            throw new IllegalArgumentException(I18nService.get("validation.required"));
        }
        if (certificateRepository.existsByCandidateId(candidateId)) {
            throw new IllegalArgumentException(I18nService.get("validation.certificate.candidateExists"));
        }
        if (certificateRepository.existsByCertificateNumber(certificateNumber)) {
            throw new IllegalArgumentException(I18nService.get("validation.certificate.numberExists"));
        }

        Certificate certificate = certificateRepository.create(new Certificate(0, candidateId, certificateNumber.trim(), issuedDate, notes == null ? "" : notes.trim()));
        if (certificate == null) {
            throw new IllegalArgumentException(I18nService.get("validation.certificate.saveFailed"));
        }
        candidateRepository.markCertificateIssued(candidateId);
        return certificate;
    }

    public ArrayList<Certificate> getAll() {
        if (SessionService.isTrainer()) {
            return certificateRepository.getAllByTrainerUserId(SessionService.getCurrentUserId());
        }
        return certificateRepository.getAll();
    }

    public ArrayList<Candidate> getReadyForCertification() {
        if (SessionService.isTrainer()) {
            return candidateRepository.getReadyForCertificationByTrainerUserId(SessionService.getCurrentUserId());
        }
        return candidateRepository.getReadyForCertification();
    }
}
