package org.springsource.restbucks.payment;

import org.springframework.context.ApplicationEvent;
import org.springframework.modulith.events.Externalized;

import javax.money.MonetaryAmount;

@Externalized
public record PaymentReceived(MonetaryAmount price) {}
