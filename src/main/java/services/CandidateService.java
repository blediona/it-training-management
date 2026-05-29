package services;

import exceptions.InvalidNameException;
import exceptions.InvalidPaymentException;
import models.Candidate;
import models.CandidateStatus;
import models.dto.CandidateRequestDto;
import models.dto.DashboardStats;
import models.mappers.CandidateMapper;
import models.mappers.Mapper;
import repository.CandidateRepository;
import repository.IRepository;

import java.util.ArrayList;

public class CandidateService {
    private static final String NAME_PATTERN = "^[A-Za-zÇËçë ]+$";
    private final IRepository<Candidate> candidateRepository = new CandidateRepository();
    private final CandidateRepository repository = new CandidateRepository();
    private final TrainingService trainingService = new TrainingService();
    private final Mapper<Candidate> candidateMapper = new CandidateMapper();

    public Candidate save(CandidateRequestDto candidateDto) throws InvalidNameException, InvalidPaymentException {
        if (SessionService.isTrainer()) {
            throw new IllegalArgumentException(I18nService.get("access.denied.trainer"));
        }
        this.validate(candidateDto);
        if (repository.existsByEmailAndTraining(candidateDto.getEmail(), candidateDto.getTrainingName())) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.training.duplicate"));
        }
        if (SessionService.isStaff()) {
            candidateDto = normalizeStaffCandidateRequest(candidateDto, null);
        }
        Candidate candidate = this.candidateMapper.fromDto(candidateDto);
        candidate.setTrainingId(resolveTrainingId(candidateDto.getTrainingName()));
        Candidate createdCandidate;
        try {
            createdCandidate = this.candidateRepository.create(candidate);
        } catch (IllegalStateException e) {
            if (isUniqueConstraint(e)) {
                throw new IllegalArgumentException(I18nService.get("validation.candidate.training.duplicate"), e);
            }
            throw new IllegalArgumentException(I18nService.get("validation.database.error") + " " + e.getMessage(), e);
        }
        if (createdCandidate == null) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.save.failed"));
        }
        return createdCandidate;
    }

    public Candidate update(CandidateRequestDto candidateDto, int candidateId) throws InvalidNameException, InvalidPaymentException {
        if (SessionService.isTrainer()) {
            return updateStatusOnly(candidateDto, candidateId);
        }
        this.validate(candidateDto);
        Candidate candidate = this.candidateRepository.getById(candidateId);
        if (candidate == null) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.missing"));
        }
        if (SessionService.isStaff()) {
            candidateDto = normalizeStaffCandidateRequest(candidateDto, candidate);
        }
        candidate = this.candidateMapper.fromDto(candidate, candidateDto);
        candidate.setTrainingId(resolveTrainingId(candidateDto.getTrainingName()));
        Candidate updatedCandidate;
        try {
            updatedCandidate = this.candidateRepository.update(candidate);
        } catch (IllegalStateException e) {
            if (isUniqueConstraint(e)) {
                throw new IllegalArgumentException(I18nService.get("validation.candidate.training.duplicate"), e);
            }
            throw new IllegalArgumentException(I18nService.get("validation.database.error") + " " + e.getMessage(), e);
        }
        if (updatedCandidate == null) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.save.failed"));
        }
        return updatedCandidate;
    }

    public boolean delete(int candidateId) {
        return this.candidateRepository.delete(candidateId);
    }

    public ArrayList<Candidate> getAll() {
        if (SessionService.isTrainer()) {
            return this.repository.getAllByTrainerUserId(SessionService.getCurrentUserId());
        }
        return this.repository.getAll();
    }

    public DashboardStats getDashboardStats() {
        if (SessionService.isTrainer()) {
            return this.repository.getDashboardStatsByTrainerUserId(SessionService.getCurrentUserId());
        }
        return this.repository.getDashboardStats();
    }

    private Candidate updateStatusOnly(CandidateRequestDto candidateDto, int candidateId) {
        if (!repository.belongsToTrainer(candidateId, SessionService.getCurrentUserId())) {
            throw new IllegalArgumentException(I18nService.get("access.denied.trainer"));
        }
        CandidateStatus.valueOf(candidateDto.getStatus());
        if (!repository.updateStatus(candidateId, candidateDto.getStatus())) {
            throw new IllegalArgumentException(I18nService.get("validation.candidate.save.failed"));
        }
        return repository.getById(candidateId);
    }

    private void validate(CandidateRequestDto candidateData) throws InvalidNameException, InvalidPaymentException {
        if (candidateData.getFirstName() == null || candidateData.getFirstName().trim().isEmpty()) {
            throw new InvalidNameException(candidateData.getFirstName());
        }
        if (!candidateData.getFirstName().trim().matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("validation.name.letters"));
        }
        if (candidateData.getLastName() == null || candidateData.getLastName().trim().isEmpty()) {
            throw new InvalidNameException(candidateData.getLastName());
        }
        if (!candidateData.getLastName().trim().matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(I18nService.get("validation.name.letters"));
        }
        if (candidateData.getEmail() == null || !candidateData.getEmail().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(I18nService.get("validation.email.invalid"));
        }
        if (candidateData.getPhone() == null || !candidateData.getPhone().trim().matches("^\\+383\\d{8}$")) {
            throw new IllegalArgumentException(I18nService.get("validation.phone.invalid"));
        }
        if (candidateData.getTrainingName() == null || candidateData.getTrainingName().trim().isEmpty()) {
            throw new IllegalArgumentException(I18nService.get("validation.required"));
        }
        if (!trainingService.existsByName(candidateData.getTrainingName())) {
            throw new IllegalArgumentException(I18nService.get("validation.training.notRegistered"));
        }
        if (candidateData.getPaidAmount() < 0 || candidateData.getTotalFee() < 0) {
            throw new InvalidPaymentException(I18nService.get("validation.payment.negative"));
        }
        if (candidateData.getPaidAmount() > candidateData.getTotalFee()) {
            throw new InvalidPaymentException(I18nService.get("validation.payment.tooHigh"));
        }
    }

    private int resolveTrainingId(String trainingName) {
        Integer trainingId = trainingService.findIdByName(trainingName);
        if (trainingId == null) {
            throw new IllegalArgumentException(I18nService.get("validation.training.notRegistered"));
        }
        return trainingId;
    }

    private CandidateRequestDto normalizeStaffCandidateRequest(CandidateRequestDto candidateDto, Candidate existingCandidate) {
        String status = existingCandidate == null ? CandidateStatus.REGISTERED.name() : existingCandidate.getStatus().name();
        Double price = trainingService.findPriceByName(candidateDto.getTrainingName());
        double totalFee = price == null
                ? (existingCandidate == null ? candidateDto.getTotalFee() : existingCandidate.getTotalFee())
                : price;
        double paidAmount = existingCandidate == null ? 0 : existingCandidate.getPaidAmount();
        boolean certificateIssued = existingCandidate != null && existingCandidate.isCertificateIssued();

        return new CandidateRequestDto(
                candidateDto.getFirstName(),
                candidateDto.getLastName(),
                candidateDto.getEmail(),
                candidateDto.getPhone(),
                candidateDto.getTrainingName(),
                status,
                totalFee,
                paidAmount,
                certificateIssued
        );
    }

    private boolean isUniqueConstraint(Exception e) {
        String message = e.getMessage();
        return message != null
                && (message.toUpperCase().contains("SQLITE_CONSTRAINT_UNIQUE")
                || message.toUpperCase().contains("DUPLICATE ENTRY"));
    }
}
