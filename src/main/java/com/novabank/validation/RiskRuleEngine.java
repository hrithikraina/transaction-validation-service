package com.novabank.validation;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;

class RiskRuleEngine {
    List<RuleDecision> evaluate(PaymentForValidation payment) { return List.of(new RuleDecision("velocity", true), new RuleDecision("amount", true)); }
}

final class SanctionsDecisionSelector {
    private SanctionsDecisionSelector() {}

    // Prefer name-based lookup to avoid fragile positional assumptions
    public static Optional<Decision> selectDecisionByName(List<Decision> decisions, String ruleName, Logger log) {
        if (decisions == null || decisions.isEmpty()) {
            log.warn("Rules engine returned no decisions");
            return Optional.empty();
        }
        for (Decision d : decisions) {
            if (d != null && ruleName.equals(d.getRuleName())) {
                return Optional.of(d);
            }
        }
        log.warn("Decision with ruleName='{}' not found in rules-engine response (size={})", ruleName, decisions.size());
        return Optional.empty();
    }

    // Defensive positional getter for backward compatibility (never throw IndexOutOfBounds)
    public static Optional<Decision> getDecisionAtSafe(List<Decision> decisions, int index, Logger log) {
        if (decisions == null) {
            log.error("Attempted positional access but decisions list is null");
            return Optional.empty();
        }
        if (index < 0 || index >= decisions.size()) {
            log.error("Requested decision index {} out of bounds (size={})", index, decisions.size());
            return Optional.empty();
        }
        return Optional.ofNullable(decisions.get(index));
    }
}

// Example Decision interface expected by selector
interface Decision {
    String getRuleName();
    // other decision fields/methods
}