/*
 * Copyright 2012-2016 the original author or authors.
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
package org.springsource.restbucks.engine;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.OrderPaid;

/**
 * Simple {@link OrderPaid} listener marking the according {@link Order} as in process, sleeping for 5 seconds and
 * marking the order as processed right after that.
 * 
 * @author Oliver Gierke
 * @author Stéphane Nicoll
 */
@Slf4j
@Service
@AllArgsConstructor
class Engine {

	private final @NonNull OrderRepository repository;
	private final Set<Order> ordersInProgress = Collections.newSetFromMap(new ConcurrentHashMap<Order, Boolean>());

	@Async
	@TransactionalEventListener
	public void handleOrderPaidEvent(OrderPaid event) {

		Order order = repository.findOne(event.getOrderId());
		order = repository.save(order.markInPreparation());

		ordersInProgress.add(order);

		LOG.info("Starting to process order {}.", order);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		order = repository.save(order.markPrepared());

		ordersInProgress.remove(order);

		LOG.info("Finished processing order {}.", order);
	}
}
