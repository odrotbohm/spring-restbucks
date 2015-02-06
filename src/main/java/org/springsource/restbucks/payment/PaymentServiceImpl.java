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
package org.springsource.restbucks.payment;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.Status;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.Payment.Receipt;

/**
 * Implementation of {@link PaymentService} delegating persistence operations to {@link PaymentRepository} and
 * {@link CreditCardRepository}.
 * 
 * @author Oliver Gierke
 */
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class PaymentServiceImpl implements PaymentService {

	private final @NonNull CreditCardRepository creditCardRepository;
	private final @NonNull PaymentRepository paymentRepository;
	private final @NonNull OrderRepository orderRepository;
	private final @NonNull ApplicationEventPublisher publisher;

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#pay(org.springsource.restbucks.order.Order, org.springsource.restbucks.payment.Payment)
	 */
	@Override
	public CreditCardPayment pay(Order order, CreditCardNumber creditCardNumber) {

		if (order.isPaid()) {
			throw new PaymentException(order, "Order already paid!");
		}

		CreditCard creditCard = creditCardRepository.findByNumber(creditCardNumber);

		if (creditCard == null) {
			throw new PaymentException(order, String.format("No credit card found for number: %s",
					creditCardNumber.getNumber()));
		}

		if (!creditCard.isValid()) {
			throw new PaymentException(order, String.format("Invalid credit card with number %s, expired %s!",
					creditCardNumber.getNumber(), creditCard.getExpirationDate()));
		}

		order.markPaid();
		CreditCardPayment payment = paymentRepository.save(new CreditCardPayment(creditCard, order));

		publisher.publishEvent(new OrderPaidEvent(order.getId()));

		return payment;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#getPaymentFor(org.springsource.restbucks.order.Order)
	 */
	@Override
	@Transactional(readOnly = true)
	public Payment getPaymentFor(Order order) {
		return paymentRepository.findByOrder(order);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#takeReceiptFor(org.springsource.restbucks.order.Order)
	 */
	@Override
	public Receipt takeReceiptFor(Order order) {

		order.setStatus(Status.TAKEN);
		orderRepository.save(order);

		Payment payment = getPaymentFor(order);
		return payment == null ? null : payment.getReceipt();
	}
}
