package com.novabank.validation;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Persists the processing outcome for a validation failure. */
class FailedRequestRepository {
    private static final Path DATABASE_PATH =
            Path.of("src/main/java/com/novabank/validation/PaymentsPlatform.db");

    void recordFailure(PaymentForValidation payment, RuntimeException error) {
        String jdbcUrl = "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath();

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            connection.setAutoCommit(false);
            updateRequestStatus(connection, payment);
            updateProcessingStatus(connection, payment, error);
            connection.commit();
        } catch (SQLException databaseError) {
            throw new IllegalStateException("Could not persist validation failure", databaseError);
        }
    }

    private void updateRequestStatus(Connection connection, PaymentForValidation payment)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE channel_requests SET request_status = 'FAILED', validation_timestamp = NULL "
                        + "WHERE request_id = ? AND payment_id = ?")) {
            statement.setString(1, payment.requestId());
            statement.setString(2, payment.paymentId());
            statement.executeUpdate();
        }
    }

    private void updateProcessingStatus(
            Connection connection, PaymentForValidation payment, RuntimeException error) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE processing_transactions "
                        + "SET processing_status = 'FAILED', validation_result = ?, posted_timestamp = NULL "
                        + "WHERE request_id = ? AND payment_id = ?")) {
            statement.setString(1, error.getClass().getSimpleName());
            statement.setString(2, payment.requestId());
            statement.setString(3, payment.paymentId());
            statement.executeUpdate();
        }
    }
}
