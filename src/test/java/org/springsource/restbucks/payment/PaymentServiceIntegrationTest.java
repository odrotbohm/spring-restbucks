/*
 * Copyright 2012-2015 the original author or authors.
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
import static org.springsource.restbucks.order.OrderRepositoryIntegrationTest.*;
import static org.springsource.restbucks.payment.CreditCardRepositoryIntegrationTest.*;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springsource.restbucks.AbstractIntegrationTest;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.Status;
import org.springsource.restbucks.order.OrderRepository;

/**
 * Integration tests for {@link PaymentServiceImpl}.
 * 
 * @author Oliver Gierke
 */
@DirtiesContext
public class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired PaymentService paymentService;
	@Autowired OrderRepository orders;
	@Autowired CreditCardRepository creditCards;

	@Test
	public void marksOrderAsPaid() {

		Order order = orders.save(createOrder());
		CreditCard creditCard = creditCards.save(createCreditCard());
		CreditCardPayment payment = paymentService.pay(order, creditCard.getNumber());

		assertThat(paymentService.getPaymentFor(order), is(Optional.of(payment)));
		assertThat(order.isPaid(), is(true));
	}

	@Test
	public void marksOrderAsTakenIfReceiptIsTaken() {

		Order order = orders.save(createOrder());
		order.markPaid();
		order.markInPreparation();
		order.markPrepared();
		orders.save(order);

		paymentService.takeReceiptFor(order);

		assertThat(orders.findOne(order.getId()).getStatus(), is(Status.TAKEN));
	}
}
