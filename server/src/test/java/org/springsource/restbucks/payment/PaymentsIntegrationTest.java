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
import static org.springsource.restbucks.order.OrderTestUtils.*;
import static org.springsource.restbucks.payment.CreditCardsIntegrationTest.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springsource.restbucks.AbstractIntegrationTest;
import org.springsource.restbucks.order.Orders;

/**
 * Integration tests for {@link Payments}.
 *
 * @author Oliver Gierke
 */
class PaymentsIntegrationTest extends AbstractIntegrationTest {

	@Autowired Payments payments;
	@Autowired CreditCards creditCards;
	@Autowired Orders orders;

	@Test
	void savesCreditCardPayment() {

		var creditCardNumber = createCreditCardNumber();
		var order = orders.save(createOrder());

		CreditCardPayment payment = payments.save(new CreditCardPayment(creditCardNumber, order));

		assertThat(payment.getId()).isNotNull();
		assertThat(payments.findByOrder(order.getId())).hasValue(payment);
	}
}
