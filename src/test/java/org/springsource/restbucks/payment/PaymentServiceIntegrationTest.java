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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springsource.restbucks.AbstractIntegrationTest;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;

/**
 * @author Oliver Gierke
 */
public class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	PaymentService paymentService;

	@Autowired
	OrderRepository orderRepository;
	@Autowired
	CreditCardRepository creditCardRepository;

	@Test
	public void marksOrderAsPaid() {

		Order order = orderRepository.findOne(1L);
		CreditCard creditCard = creditCardRepository.findOne(1L);

		CreditCardPayment payment = paymentService.pay(order, creditCard.getNumber());

		assertThat(paymentService.getPaymentFor(order), is((Payment) payment));
		assertThat(order.isPaid(), is(true));
	}
}
