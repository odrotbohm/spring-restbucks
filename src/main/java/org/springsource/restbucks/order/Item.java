/*
 * Copyright 2013-2015 the original author or authors.
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

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.Lob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.javamoney.moneta.Money;
import org.springsource.restbucks.core.AbstractEntity;

/**
 * A line item.
 * 
 * @author Oliver Gierke
 */
@Data
@Entity
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Item extends AbstractEntity {

	private final String name;
	private final int quantity;
	private final Milk milk;
	private final Size size;
	private final @Lob Money price;

	protected Item() {
		this(null, null);
	}

	public Item(String name, Money price) {
		this(name, 1, Milk.SEMI, Size.LARGE, price);
	}

	public MonetaryAmount getPrice() {
		return price;
	}
}
