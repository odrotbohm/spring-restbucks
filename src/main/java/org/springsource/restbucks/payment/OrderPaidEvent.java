/*
 * Copyright 2012-2014 the original author or authors.
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
package org.springsource.restbucks.payment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.springsource.restbucks.order.Order;

/**
 * Event to be thrown when an {@link Order} has been payed.
 * 
 * @author Oliver Gierke
 */
@Getter
@EqualsAndHashCode
@ToString
public class OrderPaidEvent  {

	private static final long serialVersionUID = -6150362015056003378L;
	private final long orderId;

	/**
	 * Creates a new {@link OrderPaidEvent}
	 * 
	 * @param orderId the id of the order that just has been payed
	 */
	public OrderPaidEvent(long orderId) {
		this.orderId = orderId;
	}
}
