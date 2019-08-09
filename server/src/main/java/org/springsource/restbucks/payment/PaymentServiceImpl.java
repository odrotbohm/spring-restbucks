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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.Payment.Receipt;

/**
 * Implementation of {@link PaymentService} delegating persistence operations to {@link PaymentRepository} and
 * {@link CreditCardRepository}.
 * 
 * @author Oliver Gierke
 * @author Stéphane Nicoll
 */
@Service
@Transactional
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentService {

	private final @NonNull CreditCardRepository creditCardRepository;
	private final @NonNull PaymentRepository paymentRepository;
	private final @NonNull OrderRepository orderRepository;

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#pay(org.springsource.restbucks.order.Order, org.springsource.restbucks.payment.Payment)
	 */
	@Override
	public CreditCardPayment pay(Order order, CreditCardNumber creditCardNumber) {

		if (order.isPaid()) {
			throw new PaymentException(order, "Order already paid!");
		}

		// Using Optional.orElseThrow(…) doesn't work due to https://bugs.openjdk.java.net/browse/JDK-8054569
		var creditCardResult = creditCardRepository.findByNumber(creditCardNumber);

		if (!creditCardResult.isPresent()) {
			throw new PaymentException(order,
					String.format("No credit card found for number: %s", creditCardNumber.getNumber()));
		}

		var creditCard = creditCardResult.get();

		if (!creditCard.isValid()) {
			throw new PaymentException(order, String.format("Invalid credit card with number %s, expired %s!",
					creditCardNumber.getNumber(), creditCard.getExpirationDate()));
		}

		var payment = paymentRepository.save(new CreditCardPayment(creditCard, order));

		orderRepository.save(order.markPaid());

		return payment;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#getPaymentFor(org.springsource.restbucks.order.Order)
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Payment> getPaymentFor(Order order) {
		return paymentRepository.findByOrder(order);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#takeReceiptFor(org.springsource.restbucks.order.Order)
	 */
	@Override
	public Optional<Receipt> takeReceiptFor(Order order) {

		var result = orderRepository.save(order.markTaken());

		return getPaymentFor(result).map(Payment::getReceipt);
	}
}
