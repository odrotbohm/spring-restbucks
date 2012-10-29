/*
 * Copyright 2012 the original author or authors.
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

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.Status;
import org.springsource.restbucks.payment.Payment.Receipt;

/**
 * Implementation of {@link PaymentService} delegating persistence operations to {@link PaymentRepository} and
 * {@link CreditCardRepository}.
 * 
 * @author Oliver Gierke
 */
@Service
@Transactional
class PaymentServiceImpl implements PaymentService {

	private final CreditCardRepository creditCardRepository;
	private final PaymentRepository paymentRepository;

	/**
	 * Creates a new {@link PaymentServiceImpl} from the given {@link PaymentRepository} and {@link CreditCardRepository}.
	 * 
	 * @param paymentRepository must not be {@literal null}.
	 * @param creditCardRepository must not be {@literal null}.
	 */
	@Autowired
	public PaymentServiceImpl(PaymentRepository paymentRepository, CreditCardRepository creditCardRepository) {

		Assert.notNull(paymentRepository, "PaymentRepository must not be null!");
		Assert.notNull(creditCardRepository, "CreditCardRepository must not be null!");

		this.creditCardRepository = creditCardRepository;
		this.paymentRepository = paymentRepository;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#pay(org.springsource.restbucks.order.Order, org.springsource.restbucks.payment.Payment)
	 */
	@Override
	public CreditCardPayment pay(Order order, CreditCardNumber creditCardNumber) {

		if (order.isPaid()) {
			throw new RuntimeException();
		}

		CreditCard creditCard = creditCardRepository.findByNumber(creditCardNumber);

		if (!creditCard.isValid(new LocalDate())) {
			throw new IllegalArgumentException();
		}

		CreditCardPayment payment = paymentRepository.save(new CreditCardPayment(creditCard, order));
		order.markPaid();

		return payment;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.payment.PaymentService#getPaymentFor(org.springsource.restbucks.order.Order)
	 */
	@Override
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

		Payment payment = getPaymentFor(order);
		return payment == null ? null : payment.getReceipt();
	}
}
