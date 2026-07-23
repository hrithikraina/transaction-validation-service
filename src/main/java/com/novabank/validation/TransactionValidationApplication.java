package com.novabank.validation;

import java.nio.file.Files;
import java.nio.file.Path;

public class TransactionValidationApplication {
    public static void main(String[] args) throws Exception {
        ValidationLog log = new ValidationLog();
        TransactionValidationService service = new TransactionValidationService(new RiskRuleEngine(), log);
        FailedRequestRepository failedRequestRepository = new FailedRequestRepository();

        PaymentForValidation payment =
                new PaymentForValidation("REQ-1003", "PAY-482912", 540.75);

        try {
            service.validate(payment);
        } catch (RuntimeException error) {
            failedRequestRepository.recordFailure(payment, error);
            log.error("request_id=" + payment.requestId()
                    + " payment_id=" + payment.paymentId()
                    + " error_code=" + error.getClass().getSimpleName()
                    + " " + error.getMessage(), error);
        }

        Files.createDirectories(Path.of("logs"));
        Files.writeString(Path.of("logs/transaction-validation-service.log"), log.entries());
        System.out.print(log.entries());
    }
}
