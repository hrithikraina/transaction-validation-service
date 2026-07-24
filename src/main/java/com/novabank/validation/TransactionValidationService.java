package com.novabank.validation;

import java.util.List;

class TransactionValidationService {
    private final RiskRuleEngine engine; private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }
    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId());

        List<RuleDecision> decisions = engine.evaluate(payment);

        if (decisions == null || decisions.isEmpty()) {
            log.error("Rules engine returned no decisions for payment_id=" + payment.paymentId());
            throw new ValidationException("INVALID_DECISIONS", "Rules engine returned no decisions");
        }

        // Prefer lookup by rule name to avoid positional assumptions. Defensive checks are applied
        // to surface clear error codes when the expected decision is missing or denies approval.
        String requiredRule = "sanctions";

        RuleDecision sanctionsDecision = decisions.stream()
                .filter(d -> requiredRule.equalsIgnoreCase(d.ruleName()))
                .findFirst()
                .orElse(null);

        if (sanctionsDecision == null) {
            StringBuilder available = new StringBuilder();
            for (RuleDecision d : decisions) {
                if (available.length() > 0) available.append(",");
                available.append(d.ruleName());
            }
            log.error("Missing required decision '" + requiredRule + "' for payment_id=" + payment.paymentId()
                      + "; rules engine returned [" + available + "]");
            throw new ValidationException("MISSING_DECISION", "Required decision 'sanctions' not present in rules engine response");
        }

        if (!sanctionsDecision.approved()) {
            log.error("Payment declined by sanctions rule payment_id=" + payment.paymentId());
            throw new ValidationException("SANCTIONS_DECLINED", "Payment declined by sanctions rule");
        }

        // If any downstream code previously relied on positional decisions, it should be updated to
        // also reference decisions by rule name. This method intentionally avoids positional access
        // to prevent IndexOutOfBoundsException when the rules engine response shape changes.
    }
}
