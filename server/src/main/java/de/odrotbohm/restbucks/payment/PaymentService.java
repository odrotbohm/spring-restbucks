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
package de.odrotbohm.restbucks.payment;

import de.odrotbohm.restbucks.order.Order;
import de.odrotbohm.restbucks.payment.Payment.Receipt;

import java.util.Optional;

import org.jmolecules.ddd.annotation.Service;

/**
 * Interface to collect payment services.
 *
 * @author Oliver Gierke
 */
@Service
public interface PaymentService {

	/**
	 * Pay the given {@link Order} with the {@link CreditCard} identified by the given {@link CreditCardNumber}.
	 *
	 * @param order must not be {@literal null}.
	 * @param creditCardNumber must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	CreditCardPayment pay(Order order, CreditCardNumber creditCardNumber);

	/**
	 * Returns the {@link Payment} for the given {@link Order}.
	 *
	 * @param order must not be {@literal null}.
	 * @return the {@link Payment} for the given {@link Order} or {@link Optional#empty()} if the Order hasn't been payed
	 *         yet.
	 */
	Optional<Payment<?>> getPaymentFor(Order order);

	/**
	 * Takes the receipt
	 *
	 * @param order must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	Optional<Receipt> takeReceiptFor(Order order);
}
