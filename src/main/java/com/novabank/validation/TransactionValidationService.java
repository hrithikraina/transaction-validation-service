package com.novabank.validation;

import java.util.List;
import java.util.Optional;

class TransactionValidationService {
    private final RiskRuleEngine engine; private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }
    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId());

        List<RuleDecision> decisions = engine.evaluate(payment);

        if (decisions == null) {
            // defensive: engine should not return null, but guard to avoid NPEs / unexpected crashes
            log.info("ERROR: No decisions returned by RiskRuleEngine payment_id=" + payment.paymentId());
            throw new IllegalStateException("Validation failed: no rule decisions available");
        }

        // Defensive lookup: find the sanctions decision by ruleName instead of relying on a fixed index
        Optional<RuleDecision> sanctionsOpt = decisions.stream()
            .filter(d -> d != null && "sanctions".equalsIgnoreCase(safeRuleName(d)))
            .findFirst();

        if (!sanctionsOpt.isPresent()) {
            // Clear, audit-friendly logging for missing decision case
            log.info("ERROR: Sanctions decision missing from risk decisions payment_id=" + payment.paymentId() + " decisions_count=" + decisions.size());
            throw new IllegalStateException("Validation failed: sanctions decision missing");
        }

        RuleDecision sanctionsDecision = sanctionsOpt.get();
        if (!sanctionsDecision.approved()) {
            log.info("Payment declined by sanctions rule payment_id=" + payment.paymentId());
            throw new IllegalStateException("Payment declined");
        }
    }

    // Helper to safely read a decision's rule name without risking a runtime exception if the implementation differs
    private static String safeRuleName(RuleDecision d) {
        try {
            return d.ruleName();
        } catch (Throwable t) {
            return null;
        }
    }
}
