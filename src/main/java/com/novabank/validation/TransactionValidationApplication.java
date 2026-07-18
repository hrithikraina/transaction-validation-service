package com.novabank.validation;

import java.nio.file.Files;
import java.nio.file.Path;

public class TransactionValidationApplication {
    public static void main(String[] args) throws Exception {
        ValidationLog log = new ValidationLog();
        TransactionValidationService service = new TransactionValidationService(new RiskRuleEngine(), log);
        try { service.validate(new PaymentForValidation("PAY-90021", 1250.00)); }
        catch (IndexOutOfBoundsException error) { log.error("java.lang.IndexOutOfBoundsException: " + error.getMessage() + " while selecting sanctions rule for payment_id=PAY-90021"); }
        Files.createDirectories(Path.of("logs")); Files.writeString(Path.of("logs/transaction-validation-service.log"), log.entries()); System.out.print(log.entries());
    }
}
