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
package de.odrotbohm.restbucks.order;

import static de.odrotbohm.restbucks.order.Order.Status.*;
import static de.odrotbohm.restbucks.order.OrderTestUtils.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for Spring Data based {@link Orders}.
 *
 * @author Oliver Gierke
 */
@Transactional
@ApplicationModuleTest(mode = BootstrapMode.DIRECT_DEPENDENCIES)
class OrdersIntegrationTest {

	@Autowired Orders repository;

	@Test
	void findsAllOrders() {

		Iterable<Order> orders = repository.findAll();
		assertThat(orders).isNotEmpty();
	}

	@Test
	void createsNewOrder() {

		Long before = repository.count();

		Order order = repository.save(createOrder());

		Iterable<Order> result = repository.findAll();

		assertThat(result).hasSize(before.intValue() + 1);
		assertThat(result).contains(order);
	}

	@Test
	void findsOrderByStatus() {

		int paidBefore = repository.findByStatus(PAID).size();
		int paymentExpectedBefore = repository.findByStatus(PAYMENT_EXPECTED).size();

		Order order = repository.save(createOrder());
		assertThat(repository.findByStatus(PAYMENT_EXPECTED)).hasSize(paymentExpectedBefore + 1);
		assertThat(repository.findByStatus(PAID)).hasSize(paidBefore);

		order.markPaid();
		order = repository.save(order);

		assertThat(repository.findByStatus(PAYMENT_EXPECTED)).hasSize(paymentExpectedBefore);
		assertThat(repository.findByStatus(PAID)).hasSize(paidBefore + 1);
	}
}
