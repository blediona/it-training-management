package repository;

import models.Certificate;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class CertificateRepository {
    public Certificate create(Certificate certificate) {
        String query = "INSERT INTO certificates(candidate_id, certificate_number, issued_date, notes) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, certificate.getCandidateId());
            pstm.setString(2, certificate.getCertificateNumber());
            pstm.setString(3, certificate.getIssuedDate().toString());
            pstm.setString(4, certificate.getNotes());
            pstm.executeUpdate();
            return certificate;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ArrayList<Certificate> getAll() {
        String query = "SELECT * FROM certificates ORDER BY issued_date DESC";
        ArrayList<Certificate> certificates = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query);
                ResultSet res = pstm.executeQuery()
        ) {
            while (res.next()) {
                certificates.add(Certificate.getFromResultSet(res));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return certificates;
    }

    public boolean existsByCertificateNumber(String certificateNumber) {
        String query = "SELECT COUNT(*) FROM certificates WHERE LOWER(certificate_number) = LOWER(?)";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, certificateNumber == null ? "" : certificateNumber.trim());
            try (ResultSet res = pstm.executeQuery()) {
                return res.next() && res.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean existsByCandidateId(int candidateId) {
        String query = "SELECT COUNT(*) FROM certificates WHERE candidate_id = ?";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, candidateId);
            try (ResultSet res = pstm.executeQuery()) {
                return res.next() && res.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}