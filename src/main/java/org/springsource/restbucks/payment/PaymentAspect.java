/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks.payment;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springsource.restbucks.order.Order;

/**
 * Aspect to publish an {@link OrderPayedEvent} on successful execution of
 * {@link PaymentService#pay(Order, CreditCardNumber)} <em>after</em> the transaction has completed. Manually defines
 * the order of the aspect to be <em>before</em> the transaction aspect.
 * 
 * @author Oliver Gierke
 */
@Aspect
@Service
@Slf4j
class PaymentAspect implements ApplicationEventPublisherAware, Ordered {

	private ApplicationEventPublisher publisher;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
	 */
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE - 10;
	}

	/**
	 * Publishes an {@link OrderPayedEvent} for the given {@link Order}.
	 * 
	 * @param order
	 */
	@AfterReturning(value = "execution(* PaymentService.pay(org.springsource.restbucks.order.Order, ..)) && args(order, ..)")
	public void triggerPaymentEvent(Order order) {

		if (order == null) {
			return;
		}

		log.info("Publishing order payed event for order {}!", order.getId());
		this.publisher.publishEvent(new OrderPayedEvent(order.getId(), this));
	}
}
