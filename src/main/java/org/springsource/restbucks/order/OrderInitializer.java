/*
 * Copyright 2012-2013 the original author or authors.
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

import static org.springsource.restbucks.core.MonetaryAmount.*;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springsource.restbucks.core.MonetaryAmount;

/**
 * Initializer to set up two {@link Order}s.
 * 
 * @author Oliver Gierke
 */
@Service
class OrderInitializer {

	/**
	 * Creates two orders and persists them using the given {@link OrderRepository}.
	 * 
	 * @param orders must not be {@literal null}.
	 */
	@Autowired
	public OrderInitializer(OrderRepository orders) {

		Assert.notNull(orders, "OrderRepository must not be null!");

		orders.deleteAll();

		Item javaChip = new Item("Java Chip", new MonetaryAmount(EURO, 4.20));
		Item cappuchino = new Item("Cappuchino", new MonetaryAmount(EURO, 3.20));

		Order javaChipOrder = new Order(javaChip);
		Order cappuchinoOrder = new Order(cappuchino);

		orders.save(Arrays.asList(javaChipOrder, cappuchinoOrder));
	}
}
