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
package de.odrotbohm.restbucks.order.web;

import de.odrotbohm.restbucks.order.Order;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * {@link ResourceProcessor} implementation to add links to the {@link Order} representation that indicate that the
 * Order can be updated or cancelled as long as it has not been paid yet.
 *
 * @author Oliver Drotbohm
 */
@Component
@RequiredArgsConstructor
class CoreOrderResourceProcessor implements RepresentationModelProcessor<EntityModel<Order>> {

	public static final String CANCEL_REL = "cancel";
	public static final String UPDATE_REL = "update";

	private final @NonNull EntityLinks entityLinks;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	@Override
	public EntityModel<Order> process(EntityModel<Order> resource) {

		var typedEntityLinks = entityLinks.forType(Order::getId);
		var order = resource.getContent();
		var orderLink = typedEntityLinks.linkForItemResource(order);

		return resource
				.addIf(!order.isPaid(), () -> orderLink.withRel(CANCEL_REL))
				.addIf(!order.isPaid(), () -> orderLink.withRel(UPDATE_REL));
	}
}
