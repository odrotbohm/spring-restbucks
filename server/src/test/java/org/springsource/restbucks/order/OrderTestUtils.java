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

import static org.springsource.restbucks.core.Currencies.*;

import org.javamoney.moneta.Money;
import org.springframework.test.util.ReflectionTestUtils;
import org.springsource.restbucks.drinks.Drink;
import org.springsource.restbucks.drinks.Milk;
import org.springsource.restbucks.drinks.Size;
import org.springsource.restbucks.order.Order.Status;

/**
 * Utility methods for testing.
 *
 * @author Oliver Gierke
 */
public class OrderTestUtils {

	public static Order createExistingOrder() {
		return new Order();
	}

	public static Order createExistingOrderWithStatus(Status status) {

		Order order = createExistingOrder();
		ReflectionTestUtils.setField(order, "status", status);
		return order;
	}

	public static Order createOrder() {
		return new Order().add(new Drink("English breakfast", Milk.WHOLE, Size.LARGE, Money.of(2.70, EURO)));
	}

	public static Order createPaidOrder() {
		return createOrder().markPaid();
	}

	public static Order createPreparedOrder() {

		return createPaidOrder()
				.markInPreparation()
				.markPrepared();
	}
}
