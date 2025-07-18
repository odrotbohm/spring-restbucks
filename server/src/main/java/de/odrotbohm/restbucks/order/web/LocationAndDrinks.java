/*
 * Copyright 2021 the original author or authors.
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
package de.odrotbohm.restbucks.order.web;

import de.odrotbohm.restbucks.drinks.Drink;
import de.odrotbohm.restbucks.order.Location;
import de.odrotbohm.restbucks.order.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * DTO to bind incoming request data.
 *
 * @author Oliver Drotbohm
 */
@Data
public class LocationAndDrinks {

	@NotNull //
	private Location location;
	private List<Drink> drinks;

	public Order toOrder() {

		Order order = new Order(Collections.emptyList(), location);
		drinks.forEach(order::add);

		return order;
	}
}
