/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odrotbohm.restbucks.order.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * {@link RepresentationModelProcessor} to add an {@link de.odrotbohm.restbucks.order.Order} creation link to the root
 * resource.
 *
 * @author Oliver Drotbohm
 */
@Component
@PrimaryAdapter
class RootResourceModelProcessor implements RepresentationModelProcessor<RepositoryLinksResource> {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource model) {

		return model.mapLink(LinkRelation.of("orders"), link -> {
			return link.andAffordance(afford(methodOn(OrderController.class).placeOrder(null, null, null)));
		});
	}
}
