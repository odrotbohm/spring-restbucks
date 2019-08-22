/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks.payment.web;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.transaction.annotation.Transactional;
import org.springsource.restbucks.order.Order.Status;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.CreditCardNumber;
import org.springsource.restbucks.payment.web.PaymentController.PaymentForm;

/**
 * Integration test for {@link PaymentController}.
 *
 * @author Oliver Drotbohm
 */
@Transactional
@SpringBootTest
class PaymentControllerIntegrationTest {

	@Autowired PaymentController controller;
	@Autowired OrderRepository orders;

	@Test
	void processesPayment() throws Exception {

		// Given
		var order = orders.findByStatus(Status.PAYMENT_EXPECTED).get(0);

		// When
		var model = new PaymentForm(new CreditCardNumber("1234123412341234"));
		var entity = controller.submitPayment(order, model);

		// Then
		assertThat(entity.getHeaders().getLocation()).isNotNull();
		assertThat(entity.getBody()).isInstanceOfSatisfying(RepresentationModel.class, it -> {
			assertThat(it.hasLink("order")).isTrue();
		});
	}
}
