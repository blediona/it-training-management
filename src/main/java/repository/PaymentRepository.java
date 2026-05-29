package repository;

import models.Payment;
import services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class PaymentRepository {
    public Payment create(Payment payment) {
        String query = "INSERT INTO payments(candidate_id, amount, payment_date, method) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query)
        ) {
            pstm.setInt(1, payment.getCandidateId());
            pstm.setDouble(2, payment.getAmount());
            pstm.setString(3, payment.getPaymentDate().toString());
            pstm.setString(4, payment.getMethod());
            pstm.executeUpdate();
            return payment;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ArrayList<Payment> getAll() {
        String query = "SELECT * FROM payments ORDER BY payment_date DESC";
        ArrayList<Payment> payments = new ArrayList<>();
        try (
                Connection connection = DatabaseService.getConnection();
                PreparedStatement pstm = connection.prepareStatement(query);
                ResultSet res = pstm.executeQuery()
        ) {
            while (res.next()) {
                payments.add(Payment.getFromResultSet(res));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return payments;
    }
}
