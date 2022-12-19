/*
 * Copyright 2013-2019 the original author or authors.
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.UUID;

import javax.money.MonetaryAmount;

import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.springsource.restbucks.drinks.Drink;
import org.springsource.restbucks.drinks.Milk;
import org.springsource.restbucks.drinks.Size;
import org.springsource.restbucks.drinks.Drink.DrinkIdentifier;
import org.springsource.restbucks.order.LineItem.LineItemIdentifier;

/**
 * A line item.
 *
 * @author Oliver Gierke
 */
@Getter
@AllArgsConstructor
public class LineItem implements Entity<Order, LineItemIdentifier> {

	private final LineItemIdentifier id;
	private final String name;
	private final Milk milk;
	private final Size size;
	private final MonetaryAmount price;
	private final Association<Drink, DrinkIdentifier> drink;
	private int quantity;

	public LineItem(Drink drink) {

		this.id = LineItemIdentifier.of(UUID.randomUUID().toString());
		this.name = drink.getName();
		this.quantity = 1;
		this.milk = drink.getMilk();
		this.size = drink.getSize();
		this.price = drink.getPrice();
		this.drink = Association.forAggregate(drink);
	}

	boolean refersTo(Drink drink) {
		return this.drink.getId().equals(drink.getId());
	}

	LineItem increaseAmount() {
		this.quantity++;
		return this;
	}

	@Value(staticConstructor = "of")
	public static class LineItemIdentifier implements Identifier {
		String id;
	}
}
