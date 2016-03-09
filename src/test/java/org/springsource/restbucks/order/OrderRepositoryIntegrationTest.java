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
package org.springsource.restbucks.order;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springsource.restbucks.core.Currencies.*;
import static org.springsource.restbucks.order.Order.Status.*;

import org.hamcrest.Matchers;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springsource.restbucks.AbstractIntegrationTest;

/**
 * Integration tests for Spring Data based {@link OrderRepository}.
 * 
 * @author Oliver Gierke
 */
@DirtiesContext
public class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Autowired OrderRepository repository;

	@Test
	public void findsAllOrders() {

		Iterable<Order> orders = repository.findAll();
		assertThat(orders, is(not(emptyIterable())));
	}

	@Test
	public void createsNewOrder() {

		Long before = repository.count();

		Order order = repository.save(createOrder());

		Iterable<Order> result = repository.findAll();
		assertThat(result, is(Matchers.<Order> iterableWithSize(before.intValue() + 1)));
		assertThat(result, hasItem(order));
	}

	@Test
	public void findsOrderByStatus() {

		int paidBefore = repository.findByStatus(PAID).size();
		int paymentExpectedBefore = repository.findByStatus(PAYMENT_EXPECTED).size();

		Order order = repository.save(createOrder());
		assertThat(repository.findByStatus(PAYMENT_EXPECTED).size(), is(paymentExpectedBefore + 1));
		assertThat(repository.findByStatus(PAID).size(), is(paidBefore));

		order.markPaid();
		order = repository.save(order);

		assertThat(repository.findByStatus(PAYMENT_EXPECTED), hasSize(paymentExpectedBefore));
		assertThat(repository.findByStatus(PAID), hasSize(paidBefore + 1));
	}

	public static Order createOrder() {
		return new Order(new LineItem("English breakfast", Money.of(2.70, EURO)));
	}
}
