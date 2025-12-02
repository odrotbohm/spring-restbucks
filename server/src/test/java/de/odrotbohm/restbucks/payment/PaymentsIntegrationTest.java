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

import static de.odrotbohm.restbucks.payment.CreditCardsIntegrationTest.*;
import static org.assertj.core.api.Assertions.*;

import de.odrotbohm.restbucks.AbstractIntegrationTest;
import de.odrotbohm.restbucks.order.Order.OrderIdentifier;
import de.odrotbohm.restbucks.order.Orders;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link Payments}.
 *
 * @author Oliver Drotbohm
 */
class PaymentsIntegrationTest extends AbstractIntegrationTest {

	@Autowired Payments payments;
	@Autowired CreditCards creditCards;
	@Autowired Orders orders;

	@Test
	void savesCreditCardPayment() {

		var creditCardNumber = createCreditCardNumber();
		var orderIdentifier = new OrderIdentifier(UUID.randomUUID());

		CreditCardPayment payment = payments.save(new CreditCardPayment(creditCardNumber, orderIdentifier));

		assertThat(payment.getId()).isNotNull();
		assertThat(payments.findByOrder(orderIdentifier)).hasValue(payment);
	}
}
