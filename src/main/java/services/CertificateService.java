package services;

import models.Certificate;
import models.Candidate;
import repository.CandidateRepository;
import repository.CertificateRepository;

import java.time.LocalDate;
import java.util.ArrayList;

public class CertificateService {
    private final CertificateRepository certificateRepository = new CertificateRepository();
    private final CandidateRepository candidateRepository = new CandidateRepository();

    public Certificate save(int candidateId, String certificateNumber, LocalDate issuedDate, String notes) {
        if (candidateRepository.getById(candidateId) == null) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.missing"));
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
        return certificateRepository.getAll();
    }

    public ArrayList<Candidate> getReadyForCertification() {
        return candidateRepository.getReadyForCertification();
    }
}
