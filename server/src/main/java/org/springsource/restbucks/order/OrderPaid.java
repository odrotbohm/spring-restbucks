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
package org.springsource.restbucks.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.jmolecules.event.types.DomainEvent;
import org.springsource.restbucks.order.Order.OrderIdentifier;

/**
 * Event to be thrown when an {@link Order} has been payed.
 *
 * @author Oliver Gierke
 * @author Stéphane Nicoll
 */
@Getter
@EqualsAndHashCode
@ToString
public class OrderPaid implements DomainEvent {

	private final OrderIdentifier orderId;

	/**
	 * Creates a new {@link OrderPaid}
	 *
	 * @param orderId the id of the order that just has been payed
	 */
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES) // Needed for republishing events from registry
	public OrderPaid(@JsonProperty OrderIdentifier orderId) {
		this.orderId = orderId;
	}
}
