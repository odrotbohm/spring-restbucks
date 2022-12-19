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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Orders;
import org.springsource.restbucks.payment.Payment.Receipt;

/**
 * Implementation of {@link PaymentService} delegating persistence operations to {@link Payments} and
 * {@link CreditCards}.
 *
 * @author Oliver Gierke
 * @author Stéphane Nicoll
 */
@Service
@Transactional
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentService {

	private final @NonNull CreditCards cards;
	private final @NonNull Payments payments;
	private final @NonNull Orders orders;

	/*
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#pay(org.springsource.restbucks.order.Order, org.springsource.restbucks.payment.Payment)
	 */
	@Override
	public CreditCardPayment pay(Order order, CreditCardNumber creditCardNumber) {

		if (order.isPaid()) {
			throw new PaymentFailed(order, "Order already paid!");
		}

		// Using Optional.orElseThrow(…) doesn't work due to https://bugs.openjdk.java.net/browse/JDK-8054569
		var creditCard = cards.findByNumber(creditCardNumber)
				.orElseThrow(() -> new PaymentFailed(order,
						String.format("No credit card found for number: %s", creditCardNumber)));

		if (!creditCard.isValid()) {
			throw new PaymentFailed(order, "Invalid credit card with number %s, expired %s!".formatted(
					creditCardNumber, creditCard.getExpirationDate()));
		}

		orders.markPaid(order);

		return payments.save(new CreditCardPayment(creditCardNumber, order));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#getPaymentFor(org.springsource.restbucks.order.Order)
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Payment<?>> getPaymentFor(Order order) {
		return payments.findByOrder(order.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#takeReceiptFor(org.springsource.restbucks.order.Order)
	 */
	@Override
	public Optional<Receipt> takeReceiptFor(Order order) {

		var result = orders.markTaken(order);

		return getPaymentFor(result).map(Payment::getReceipt);
	}
}
