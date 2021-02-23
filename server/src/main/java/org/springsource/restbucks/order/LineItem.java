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
import lombok.Data;
import lombok.Value;

import java.util.UUID;

import javax.money.MonetaryAmount;

import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.springsource.restbucks.order.LineItem.LineItemIdentifier;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * A line item.
 *
 * @author Oliver Gierke
 */
@Data
@AllArgsConstructor
public class LineItem implements Entity<Order, LineItemIdentifier> {

	private final LineItemIdentifier id;
	private final String name;
	private final int quantity;
	private final Milk milk;
	private final Size size;
	private final MonetaryAmount price;

	@JsonCreator
	public LineItem(String name, MonetaryAmount price) {
		this(LineItemIdentifier.of(UUID.randomUUID()), name, 1, Milk.SEMI, Size.LARGE, price);
	}

	@Value(staticConstructor = "of")
	public static class LineItemIdentifier implements Identifier {
		UUID id;
	}
}
