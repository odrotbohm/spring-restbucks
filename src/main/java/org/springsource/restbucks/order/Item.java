/*
 * Copyright 2013 the original author or authors.
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

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springsource.restbucks.core.AbstractEntity;
import org.springsource.restbucks.core.MonetaryAmount;

/**
 * @author Oliver Gierke
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Item extends AbstractEntity {

	private String name;
	private int quantity;
	private Milk milk;
	private Size size;
	private MonetaryAmount price;

	protected Item() {

	}

	public Item(String name, MonetaryAmount price) {
		this(name, 1, Milk.SEMI, Size.LARGE, price);
	}

	/**
	 * @param name
	 * @param quantity
	 * @param milk
	 * @param size
	 * @param cost
	 */
	public Item(String name, int quantity, Milk milk, Size size, MonetaryAmount price) {

		this.name = name;
		this.quantity = quantity;
		this.milk = milk;
		this.size = size;
		this.price = price;
	}
}
