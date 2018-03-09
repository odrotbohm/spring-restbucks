/*
 * Copyright 2012-2016 the original author or authors.
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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import org.glassfish.jersey.server.internal.scanning.ResourceProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderService;

/**
 * {@link ResourceProcessor} to enrich {@link Order} {@link EntityModel}s with links to the {@link PaymentController}.
 *
 * @author Oliver Gierke
 */
@Component
@RequiredArgsConstructor
class PaymentOrderResourceProcessor implements RepresentationModelProcessor<EntityModel<Order>> {

	private final @NonNull PaymentLinks paymentLinks;

	@Autowired private OrderService orderService;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	@Override
	public EntityModel<Order> process(EntityModel<Order> resource) {

		Order order = resource.getContent();

		Collection<String> links = orderService.getPossibleLinks(order, "PAYMENT");

		if (links.contains("pay")) {
			resource.add(paymentLinks.getPaymentLink(order));
		}

		if (links.contains("receipt")) {
			resource.add(paymentLinks.getReceiptLink(order));
		}

		return resource;
	}
}
