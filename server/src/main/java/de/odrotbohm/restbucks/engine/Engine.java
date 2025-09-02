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
package de.odrotbohm.restbucks.engine;

import de.odrotbohm.restbucks.order.Order;
import de.odrotbohm.restbucks.order.Order.OrderPaid;
import de.odrotbohm.restbucks.order.Orders;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jmolecules.ddd.annotation.Service;
import org.springframework.modulith.events.ApplicationModuleListener;

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

	private final @NonNull Orders orders;
	private final @NonNull EngineSettings settings;
	private final Set<Order> ordersInProgress = Collections.newSetFromMap(new ConcurrentHashMap<Order, Boolean>());

	@ApplicationModuleListener
	public void handleOrderPaidEvent(OrderPaid event) {

		var order = orders.markInPreparation(event.orderIdentifier());
		var processingTime = settings.getProcessingTime();

		ordersInProgress.add(order);

		LOG.info("Starting to process order {} for {}.", order, processingTime.toString());

		try {
			Thread.sleep(processingTime.toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		order = orders.markPrepared(order);

		ordersInProgress.remove(order);

		LOG.info("Finished processing order {}.", order);
	}
}
