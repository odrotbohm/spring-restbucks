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
package org.springsource.restbucks.order.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.springsource.restbucks.order.Order;

/**
 * {@link ResourceProcessor} implementation to add links to the {@link Order} representation that indicate that the
 * Order can be updated or cancelled as long as it has not been paid yet.
 * 
 * @author Oliver Gierke
 */
@Component
@RequiredArgsConstructor
class CoreOrderResourceProcessor implements ResourceProcessor<Resource<Order>> {

	public static final String CANCEL_REL = "cancel";
	public static final String UPDATE_REL = "update";

	private final @NonNull EntityLinks entityLinks;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceProcessor#process(org.springframework.hateoas.ResourceSupport)
	 */
	@Override
	public Resource<Order> process(Resource<Order> resource) {

		Order order = resource.getContent();

		if (!order.isPaid()) {
			resource.add(entityLinks.linkForSingleResource(order).withRel(CANCEL_REL));
			resource.add(entityLinks.linkForSingleResource(order).withRel(UPDATE_REL));
		}

		return resource;
	}
}
