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
package org.springsource.restbucks.payment.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springsource.restbucks.order.Order;

/**
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
	 * @param entityLinks
	 */
	@Autowired
	public PaymentLinks(EntityLinks entityLinks) {
		this.entityLinks = entityLinks;
	}

	Link getPaymentLink(Order order) {
		return entityLinks.linkForSingleResource(order).slash(PAYMENT).withRel(PAYMENT_REL);
	}

	Link getReceiptLink(Order order) {
		return entityLinks.linkForSingleResource(order).slash(RECEIPT).withRel(RECEIPT_REL);
	}
}
