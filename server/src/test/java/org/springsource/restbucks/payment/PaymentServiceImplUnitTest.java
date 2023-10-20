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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderTestUtils;
import org.springsource.restbucks.order.Orders;

/**
 * Unit tests for {@link PaymentServiceImpl}.
 *
 * @author Oliver Gierke
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplUnitTest {

	static final CreditCardNumber NUMBER = CreditCardNumber.of("1234123412341234");

	PaymentService paymentService;

	@Mock Payments paymentRepository;
	@Mock CreditCards creditCardRepository;
	@Mock Orders orderRepository;

	@BeforeEach
	void setUp() {
		this.paymentService = new PaymentServiceImpl(creditCardRepository, paymentRepository, orderRepository);
	}

	@Test
	void rejectsNullPaymentRepository() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new PaymentServiceImpl(creditCardRepository, null, orderRepository));
	}

	@Test
	void rejectsNullCreditCardRepository() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new PaymentServiceImpl(null, paymentRepository, orderRepository));
	}

	@Test
	void rejectsAlreadyPaidOrder() {

		var order = OrderTestUtils.createPaidOrder();

		assertThatExceptionOfType(PaymentFailed.class) //
				.isThrownBy(() -> paymentService.pay(order, NUMBER)) //
				.withMessageContaining("paid");
	}

	@Test
	void rejectsPaymentIfNoCreditCardFound() {

		// when(creditCardRepository.findByNumber(NUMBER)).thenReturn(Optional.empty());

		assertThatExceptionOfType(PaymentFailed.class) //
				.isThrownBy(() -> paymentService.pay(new Order(), NUMBER)) //
				.withMessageContaining("credit card") //
				.withMessageContaining(NUMBER.getNumber());
	}
}
