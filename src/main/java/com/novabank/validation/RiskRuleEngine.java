package com.novabank.validation;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RiskRuleEngine {
    List<RuleDecision> evaluate(PaymentForValidation payment) {
        return List.of(new RuleDecision("velocity", true), new RuleDecision("amount", true));
    }
}

final class RiskDecisionHelper {
    private static final Logger log = LoggerFactory.getLogger(RiskDecisionHelper.class);

    private RiskDecisionHelper() { }

    // Find a decision by its rule name instead of relying on positional index
    public static Optional<RuleDecision> findDecisionByName(List<RuleDecision> decisions, String ruleName) {
        if (decisions == null || decisions.isEmpty() || ruleName == null) {
            return Optional.empty();
        }
        return decisions.stream()
                        .filter(d -> ruleName.equals(d.getRuleName()))
                        .findFirst();
    }

    // Caller should handle empty Optional; this helper avoids IndexOutOfBoundsException
    public static Optional<RuleDecision> findSanctionsDecision(List<RuleDecision> decisions) {
        Optional<RuleDecision> opt = findDecisionByName(decisions, "sanctions");
        if (!opt.isPresent()) {
            log.warn("Sanctions decision not present in rules-engine response (size={})", decisions == null ? 0 : decisions.size());
        }
        return opt;
    }
}