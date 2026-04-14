package com.bookingplatform.service.discount.rule;

import com.bookingplatform.entity.OfferRule;
import com.bookingplatform.enums.RuleType;
import com.bookingplatform.service.discount.DiscountContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

/**
 * Evaluates DAY_OF_WEEK rules.
 * rule_value JSON: {"days":["SAT","SUN"]}  (uses MON/TUE/WED/THU/FRI/SAT/SUN abbreviations)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DayOfWeekRuleEvaluator implements RuleEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public RuleType supportedType() {
        return RuleType.DAY_OF_WEEK;
    }

    @Override
    public boolean evaluate(OfferRule rule, DiscountContext context) {
        try {
            JsonNode node = objectMapper.readTree(rule.getRuleValue());
            Set<String> allowed = new HashSet<>();
            node.get("days").forEach(d -> allowed.add(d.asText().toUpperCase()));
            DayOfWeek dow = context.getShow().getShowDate().getDayOfWeek();
            String abbr = dow.name().substring(0, 3);
            return allowed.contains(abbr);
        } catch (Exception e) {
            log.warn("Failed to evaluate DAY_OF_WEEK rule {}: {}", rule.getRuleValue(), e.getMessage());
            return false;
        }
    }
}
