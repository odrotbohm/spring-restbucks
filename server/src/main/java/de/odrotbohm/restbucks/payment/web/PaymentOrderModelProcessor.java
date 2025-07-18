/*
 * Copyright 2012-2021 the original author or authors.
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
package de.odrotbohm.restbucks.payment.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import de.odrotbohm.restbucks.order.Order;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * {@link ResourceProcessor} to enrich {@link Order} {@link Resource}s with links to the {@link PaymentController}.
 *
 * @author Oliver Gierke
 */
@Component
@RequiredArgsConstructor
class PaymentOrderModelProcessor implements RepresentationModelProcessor<EntityModel<Order>> {

	private final @NonNull PaymentLinks paymentLinks;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	@Override
	public EntityModel<Order> process(EntityModel<Order> model) {

		var order = model.getContent();

		if (order == null) {
			return model;
		}

		var controller = methodOn(PaymentController.class);

		Function<Link, Link> mapper = link -> link
				.andAffordance(afford(controller.submitPayment(order, null)));

		return model
				.mapLinkIf(!order.isPaid(), IanaLinkRelations.SELF, mapper)
				.addIf(!order.isPaid(), () -> paymentLinks.getPaymentLink(order))
				.addIf(order.isReady(), () -> paymentLinks.getReceiptLink(order)
						.andAffordance(afford(controller.takeReceipt(order))));
	}
}
