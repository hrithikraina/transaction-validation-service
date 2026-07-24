package com.novabank.validation;
import java.util.List;
class TransactionValidationService {
    private final RiskRuleEngine engine;
    private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }
    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId());

        List<RuleDecision> decisions = engine.evaluate(payment);

        if (decisions == null || decisions.size() < 3) {
            throw new ValidationException("VALIDATION_FAILED", "Insufficient rule decisions: expected at least 3 but got " + (decisions == null ? "null" : decisions.size()));
        }

        RuleDecision sanctionsDecision = decisions.get(2);
        if (sanctionsDecision == null) {
            throw new ValidationException("VALIDATION_FAILED", "Sanctions decision missing (null) in decisions list");
        }

        if (!sanctionsDecision.approved()) throw new ValidationException("VALIDATION_FAILED", "Payment declined by sanctions decision");
    }
}
