/*
 * Copyright 2012-2013 the original author or authors.
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
package org.springsource.restbucks.payment.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.payment.Payment;
import org.springsource.restbucks.payment.Payment.Receipt;

/**
 * Helper component to create links to the {@link Payment} and {@link Receipt}.
 * 
 * @author Oliver Gierke
 */
@Component
public class PaymentLinks {

	static final String PAYMENT = "/payment";
	static final String RECEIPT = "/receipt";
	static final String PAYMENT_REL = "payment";
	static final String RECEIPT_REL = "receipt";

	private final EntityLinks entityLinks;

	/**
	 * Creates a new {@link PaymentLinks} instance using the given {@link EntityLinks}.
	 * 
	 * @param entityLinks must not be {@literal null}.
	 */
	@Autowired
	public PaymentLinks(EntityLinks entityLinks) {

		Assert.notNull(entityLinks, "EntityLinks must not be null!");
		this.entityLinks = entityLinks;
	}

	/**
	 * Returns the {@link Link} to point to the {@link Payment} for the given {@link Order}.
	 * 
	 * @param order must not be {@literal null}.
	 * @return
	 */
	Link getPaymentLink(Order order) {
		return entityLinks.linkForSingleResource(order).slash(PAYMENT).withRel(PAYMENT_REL);
	}

	/**
	 * Returns the {@link Link} to the {@link Receipt} of the given {@link Order}.
	 * 
	 * @param order
	 * @return
	 */
	Link getReceiptLink(Order order) {
		return entityLinks.linkForSingleResource(order).slash(RECEIPT).withRel(RECEIPT_REL);
	}
}
