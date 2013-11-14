/*
 * Copyright 2012-2013 the original author or authors.
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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.OrderPaidEvent;

/**
 * Simple {@link ApplicationListener} implementation that listens to {@link OrderPaidEvent}s marking the according
 * {@link Order} as in process, sleeping for 10 seconds and marking the order as processed right after that.
 * 
 * @author Oliver Gierke
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
class Engine implements ApplicationListener<OrderPaidEvent>, InProgressAware {

	private final @NonNull OrderRepository repository;
	private final Set<Order> ordersInProgress = Collections.newSetFromMap(new ConcurrentHashMap<Order, Boolean>());

	/*
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.engine.InProgressAware#getOrders()
	 */
	@Override
	public Set<Order> getOrders() {
		return ordersInProgress;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Async
	@Override
	public void onApplicationEvent(OrderPaidEvent event) {

		Order order = repository.findOne(event.getOrderId());
		order.markInPreparation();
		order = repository.save(order);

		ordersInProgress.add(order);

		log.info("Starting to process order {}.", order);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		order.markPrepared();
		repository.save(order);

		ordersInProgress.remove(order);

		log.info("Finished processing order {}.", order);
	}
}
