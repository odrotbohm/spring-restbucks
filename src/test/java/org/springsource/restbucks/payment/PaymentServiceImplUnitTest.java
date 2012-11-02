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

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;

/**
 * Unit tests for {@link PaymentServiceImpl}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceImplUnitTest {

	static final CreditCardNumber NUMBER = new CreditCardNumber("1234123412341234");

	PaymentService paymentService;

	@Mock
	PaymentRepository paymentRepository;
	@Mock
	CreditCardRepository creditCardRepository;
	@Mock
	OrderRepository orderRepository;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		this.paymentService = new PaymentServiceImpl(paymentRepository, creditCardRepository, orderRepository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullPaymentRepository() {
		new PaymentServiceImpl(null, creditCardRepository, orderRepository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullCreditCardRepository() {
		new PaymentServiceImpl(paymentRepository, null, orderRepository);
	}

	@Test
	public void rejectsAlreadyPaidOrder() {

		Order order = new Order();
		order.markPaid();

		exception.expect(PaymentException.class);
		exception.expectMessage("paid");

		paymentService.pay(order, NUMBER);
	}

	@Test
	public void rejectsPaymentIfNoCreditCardFound() {

		when(creditCardRepository.findByNumber(NUMBER)).thenReturn(null);

		exception.expect(PaymentException.class);
		exception.expectMessage("credit card");
		exception.expectMessage(NUMBER.getNumber());

		paymentService.pay(new Order(), NUMBER);
	}
}
