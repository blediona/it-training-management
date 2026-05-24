package models.mappers;

import models.Candidate;
import models.dto.CandidateResponseDto;

public class CandidateResponseMapper {
    public CandidateResponseDto toResponse(Candidate candidate) {
        return new CandidateResponseDto(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getEmail(),
                candidate.getTrainingName(),
                candidate.getStatus().name(),
                candidate.getPaidAmount(),
                candidate.getBalance()
        );
    }
}
