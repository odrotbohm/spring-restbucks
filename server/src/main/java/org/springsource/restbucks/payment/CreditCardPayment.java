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
package org.springsource.restbucks.payment;

import lombok.Getter;

import org.jmolecules.ddd.types.Association;
import org.springframework.util.Assert;
import org.springsource.restbucks.order.Order;

/**
 * A {@link Payment} done through a {@link CreditCard}.
 *
 * @author Oliver Gierke
 */
@Getter
public class CreditCardPayment extends Payment<CreditCardPayment> {

	private final Association<CreditCard, CreditCardNumber> creditCardNumber;

	/**
	 * Creates a new {@link CreditCardPayment} for the given {@link CreditCardNumber} and {@link Order}.
	 *
	 * @param creditCardNumber must not be {@literal null}.
	 * @param order
	 */
	public CreditCardPayment(CreditCardNumber creditCardNumber, Order order) {

		super(order);

		Assert.notNull(creditCardNumber, "Credit card number must not be null!");

		this.creditCardNumber = Association.forId(creditCardNumber);
	}
}
