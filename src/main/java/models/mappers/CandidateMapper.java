package models.mappers;

import models.Candidate;
import models.CandidateStatus;
import models.dto.CandidateRequestDto;
import models.dto.IRequestDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class CandidateMapper implements Mapper<Candidate>{
    @Override
    public Candidate getFromResultSet(ResultSet res){
        try{
            return Candidate.getFromResultSet(res);
        } catch (SQLException e){
            System.out.println(e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public Candidate fromDto(IRequestDto dto){
        CandidateRequestDto candidateDto = (CandidateRequestDto) dto;
        return new Candidate(
                0,
                candidateDto.getFirstName(),
                candidateDto.getLastName(),
                candidateDto.getEmail(),
                candidateDto.getPhone(),
                0,
                candidateDto.getTrainingName(),
                CandidateStatus.valueOf(candidateDto.getStatus()),
                candidateDto.getTotalFee(),
                candidateDto.getPaidAmount(),
                LocalDate.now(),
                candidateDto.isCertificateIssued()
        );
    }

    @Override
    public Candidate fromDto(Candidate candidate, IRequestDto dto){
        CandidateRequestDto candidateDto = (CandidateRequestDto) dto;
        candidate.setFirstName(candidateDto.getFirstName());
        candidate.setLastName(candidateDto.getLastName());
        candidate.setEmail(candidateDto.getEmail());
        candidate.setPhone(candidateDto.getPhone());
        candidate.setTrainingName(candidateDto.getTrainingName());
        candidate.setStatus(CandidateStatus.valueOf(candidateDto.getStatus()));
        candidate.setTotalFee(candidateDto.getTotalFee());
        candidate.setPaidAmount(candidateDto.getPaidAmount());
        candidate.setCertificateIssued(candidateDto.isCertificateIssued());
        return candidate;
    }
}
