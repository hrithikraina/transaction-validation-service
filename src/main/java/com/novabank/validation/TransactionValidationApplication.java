package com.novabank.validation;

import java.nio.file.Files;
import java.nio.file.Path;

public class TransactionValidationApplication {
    public static void main(String[] args) throws Exception {
        ValidationLog log = new ValidationLog();
        TransactionValidationService service = new TransactionValidationService(new RiskRuleEngine(), log);

        PaymentForValidation payment =
                new PaymentForValidation("REQ-1003", "PAY-482912", 1250.00);

        try {
            service.validate(payment);
        } catch (ValidationException error) {
            log.error("request_id=" + payment.requestId()
                    + " payment_id=" + payment.paymentId()
                    + " error_code=" + error.errorCode()
                    + " " + error.getMessage());
        }

        Files.createDirectories(Path.of("logs"));
        Files.writeString(Path.of("logs/transaction-validation-service.log"), log.entries());
        System.out.print(log.entries());
    }
}
