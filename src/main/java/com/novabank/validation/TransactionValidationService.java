package com.novabank.validation;

import java.util.List;
import java.util.Optional;

class TransactionValidationService {
    private final RiskRuleEngine engine; private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }
    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId());

        List<RuleDecision> decisions = engine.evaluate(payment);

        Optional<RuleDecision> sanctionsDecision = findSanctionsDecision(decisions);
        if (sanctionsDecision.isEmpty()) {
            // structured diagnostic for operators: include payment id and received decisions
            log.error("Missing sanctions decision for payment=" + payment.paymentId() + " decisions=" + decisions);
            throw new ValidationException("VALIDATION_FAILED", "Missing sanctions decision");
        }

        RuleDecision sd = sanctionsDecision.get();
        if (!sd.approved()) throw new IllegalStateException("Payment declined");
    }

    // Helper: safe lookup for the sanctions decision by rule name
    static Optional<RuleDecision> findSanctionsDecision(List<RuleDecision> decisions) {
        if (decisions == null) return Optional.empty();
        return decisions.stream()
                .filter(d -> "sanctions".equalsIgnoreCase(d.ruleName()))
                .findFirst();
    }
}
