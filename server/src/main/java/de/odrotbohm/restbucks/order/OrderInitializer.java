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
package de.odrotbohm.restbucks.order;

import static de.odrotbohm.restbucks.core.Currencies.*;

import de.odrotbohm.restbucks.drinks.Drink;
import de.odrotbohm.restbucks.drinks.Drinks;
import de.odrotbohm.restbucks.drinks.Milk;
import de.odrotbohm.restbucks.drinks.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

import org.javamoney.moneta.Money;
import org.jmolecules.ddd.annotation.Service;
import org.jmolecules.event.annotation.DomainEventHandler;
import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * Initializer to set up two {@link Order}s.
 *
 * @author Oliver Drotbohm
 */
@Service
@RequiredArgsConstructor
class OrderInitializer {

	private final @NonNull Orders orders;
	private final @NonNull Drinks drinks;

	/**
	 * Creates two orders and persists them using the given {@link Orders}.
	 *
	 * @param orders must not be {@literal null}.
	 */
	@DomainEventHandler
	public void init(ApplicationReadyEvent event) {

		if (orders.count() != 0) {
			return;
		}

		var javaChip = drinks.save(new Drink("Java Chip", Milk.WHOLE, Size.LARGE, Money.of(4.20, EURO)));
		var cappuchino = drinks.save(new Drink("Cappuchino", Milk.WHOLE, Size.LARGE, Money.of(3.20, EURO)));

		Stream.of(javaChip, cappuchino)
				.map(it -> new Order().add(it))
				.forEach(orders::save);
	}
}
