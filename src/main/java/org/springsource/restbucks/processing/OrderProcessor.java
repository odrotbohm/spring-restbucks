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
package org.springsource.restbucks.processing;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;
import org.springsource.restbucks.payment.OrderPayedEvent;

/**
 * Simple {@link ApplicationListener} implementation that listens to {@link OrderPayedEvent}s marking the according
 * {@link Order} as in process, sleeping for 10 seconds and marking the order as processed right after that.
 * 
 * @author Oliver Gierke
 */
@Slf4j
@Service
class OrderProcessor implements ApplicationListener<OrderPayedEvent> {

	private final OrderRepository repository;

	/**
	 * Creates a new {@link OrderProcessor} using the given {@link OrderRepository}.
	 * 
	 * @param repository must not be {@literal null}.
	 */
	@Autowired
	public OrderProcessor(OrderRepository repository) {

		Assert.notNull(repository, "OrderRepository must not be null!");
		this.repository = repository;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Async
	@Override
	public void onApplicationEvent(OrderPayedEvent event) {

		Order order = repository.findOne(event.getOrderId());
		order.markInPreparation();
		order = repository.save(order);

		log.info("Starting to process order {}.", order);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		order.markPrepared();
		repository.save(order);

		log.info("Finished processing order {}.", order);
	}
}
