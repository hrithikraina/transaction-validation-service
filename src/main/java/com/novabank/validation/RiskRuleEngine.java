package com.novabank.validation;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RiskDecisionResolver {
    private static final Logger log = LoggerFactory.getLogger(RiskDecisionResolver.class);
    private static final String SANCTIONS_RULE_NAME = "sanctions";

    // Safely find a decision by rule name instead of relying on a positional index
    public static Optional<Decision> findSanctionsDecision(List<Decision> decisions) {
        if (decisions == null || decisions.isEmpty()) {
            log.warn("Rules-engine returned no decisions");
            return Optional.empty();
        }
        for (Decision d : decisions) {
            if (d == null) continue;
            if (SANCTIONS_RULE_NAME.equalsIgnoreCase(d.getRuleName())) {
                return Optional.of(d);
            }
        }
        log.warn("Sanctions decision not found in rules list (size={})", decisions.size());
        return Optional.empty();
    }
}

class RiskRuleEngine {
    List<RuleDecision> evaluate(PaymentForValidation payment) { return List.of(new RuleDecision("velocity", true), new RuleDecision("amount", true)); }
}