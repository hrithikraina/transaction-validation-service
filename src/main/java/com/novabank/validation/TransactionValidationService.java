package com.novabank.validation;

import java.util.List;
import java.util.Optional;

class TransactionValidationService {
    private final RiskRuleEngine engine; private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }
    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId());

        List<RuleDecision> decisions;
        try {
            decisions = engine.evaluate(payment);
        } catch (Exception e) {
            // Defensive logging for engine failures so we don't leak a raw stacktrace as an unhandled exception
            log.error("payment_id=" + payment.paymentId() + " error_code=RISK_ENGINE_ERROR error=Failed to evaluate rules", e);
            // Throw a deterministic, self-documented failure to allow downstream systems to record a consistent validation_result
            throw new IllegalStateException("RISK_ENGINE_ERROR");
        }

        if (decisions == null) {
            log.error("payment_id=" + payment.paymentId() + " error_code=MISSING_RULE_DECISIONS - engine returned null");
            throw new IllegalStateException("MISSING_RULE_DECISIONS");
        }

        log.info("payment_id=" + payment.paymentId() + " decisions_count=" + decisions.size() + " decisions=" + decisions);

        // Prefer name-based lookup for the sanctions decision instead of positional access.
        Optional<RuleDecision> sanctionsDecision = decisions.stream()
                .filter(d -> "sanctions".equalsIgnoreCase(d.ruleName()))
                .findFirst();

        if (sanctionsDecision.isEmpty()) {
            // Deterministic handling for the missing sanctions decision: log clearly and return a known failure code.
            log.error("payment_id=" + payment.paymentId() + " error_code=MISSING_RULE_DECISION - no 'sanctions' decision found, decisions_count=" + decisions.size());
            throw new IllegalStateException("MISSING_RULE_DECISION");
        }

        RuleDecision sd = sanctionsDecision.get();
        if (!sd.approved()) throw new IllegalStateException("Payment declined by sanctions");
    }
}
