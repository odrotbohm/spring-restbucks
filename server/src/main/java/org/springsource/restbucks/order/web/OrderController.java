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
package org.springsource.restbucks.order.web;

import de.odrotbohm.spring.web.model.MappedPayloads;
import lombok.RequiredArgsConstructor;

import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Orders;

/**
 * @author Oliver Drotbohm
 */
@BasePathAwareController
@RequiredArgsConstructor
public class OrderController {

	private final Orders orders;

	/**
	 * Custom handler method to customize the creation of {@link Order}s.
	 *
	 * @param payload
	 * @param errors
	 * @param assembler
	 * @return
	 */
	@PostMapping(path = "/orders")
	public HttpEntity<?> placeOrder(@RequestBody LocationAndDrinks payload, Errors errors,
			PersistentEntityResourceAssembler assembler) {

		return MappedPayloads.of(payload, errors)
				.mapIfValid(LocationAndDrinks::toOrder)
				.mapIfValid(orders::save)
				.mapIfValid(assembler::toFullResource)
				.concludeIfValid(it -> ResponseEntity.created(it.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(it));
	}
}
