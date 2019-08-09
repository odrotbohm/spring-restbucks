/*
 * Copyright 2012-2018 the original author or authors.
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

import static org.springsource.restbucks.core.Currencies.*;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.javamoney.moneta.Money;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Initializer to set up two {@link Order}s.
 * 
 * @author Oliver Gierke
 */
@Service
@RequiredArgsConstructor
class OrderInitializer {

	private final @NonNull OrderRepository orders;

	/**
	 * Creates two orders and persists them using the given {@link OrderRepository}.
	 * 
	 * @param orders must not be {@literal null}.
	 */
	@EventListener
	public void init(ApplicationReadyEvent event) {

		if (orders.count() != 0) {
			return;
		}

		var javaChip = new LineItem("Java Chip", Money.of(4.20, EURO));
		var cappuchino = new LineItem("Cappuchino", Money.of(3.20, EURO));

		orders.saveAll(List.of(new Order(javaChip), new Order(cappuchino)));
	}
}
