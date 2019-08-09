/*
 * Copyright 2012-2019 the original author or authors.
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

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.TypedEntityLinks;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springsource.restbucks.Restbucks;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.payment.Payment;
import org.springsource.restbucks.payment.Payment.Receipt;

import lombok.Getter;

/**
 * Helper component to create links to the {@link Payment} and {@link Receipt}.
 *
 * @author Oliver Gierke
 */
@Component
class PaymentLinks {

	static final String PAYMENT = "/payment";
	static final String RECEIPT = "/receipt";
	static final LinkRelation PAYMENT_REL = HalLinkRelation.curied(Restbucks.CURIE_NAMESPACE, "payment");
	static final LinkRelation RECEIPT_REL = HalLinkRelation.curied(Restbucks.CURIE_NAMESPACE, "receipt");

	private final @Getter TypedEntityLinks<Order> orderLinks;

	/**
	 * Creates a new {@link PaymentLinks} for the given {@link EntityLinks}.
	 * 
	 * @param entityLinks must not be {@literal null}.
	 */
	PaymentLinks(EntityLinks entityLinks) {

		Assert.notNull(entityLinks, "EntityLinks must not be null!");

		this.orderLinks = entityLinks.forType(Order::getId);
	}

	/**
	 * Returns the {@link Link} to point to the {@link Payment} for the given {@link Order}.
	 *
	 * @param order must not be {@literal null}.
	 * @return
	 */
	Link getPaymentLink(Order order) {
		return orderLinks.linkForItemResource(order).slash(PAYMENT).withRel(PAYMENT_REL);
	}

	/**
	 * Returns the {@link Link} to the {@link Receipt} of the given {@link Order}.
	 *
	 * @param order
	 * @return
	 */
	Link getReceiptLink(Order order) {
		return orderLinks.linkForItemResource(order).slash(RECEIPT).withRel(RECEIPT_REL);
	}
}
