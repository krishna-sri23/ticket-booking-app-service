package com.bookingplatform.service.discount.rule;

import com.bookingplatform.entity.OfferRule;
import com.bookingplatform.enums.RuleType;
import com.bookingplatform.service.discount.DiscountContext;

/**
 * Strategy interface: one implementation per rule type.
 * Each evaluator knows how to check whether its rule is satisfied.
 */
public interface RuleEvaluator {
    RuleType supportedType();
    boolean evaluate(OfferRule rule, DiscountContext context);
}
