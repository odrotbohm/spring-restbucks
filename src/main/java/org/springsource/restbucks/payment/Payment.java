/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springsource.restbucks.payment;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;
import org.springsource.restbucks.core.AbstractEntity;
import org.springsource.restbucks.order.Order;

/**
 * Baseclass for payment implementations.
 * 
 * @author Oliver Gierke
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@ToString(callSuper = true)
public abstract class Payment extends AbstractEntity {

	@JoinColumn(name = "rborder") //
	@OneToOne(cascade = CascadeType.MERGE) //
	private final Order order;
	private final LocalDateTime paymentDate;

	protected Payment() {

		this.order = null;
		this.paymentDate = null;
	}

	/**
	 * Creates a new {@link Payment} referring to the given {@link Order}.
	 * 
	 * @param order must not be {@literal null}.
	 */
	public Payment(Order order) {

		Assert.notNull(order, "Order must not be null!");

		this.order = order;
		this.paymentDate = LocalDateTime.now();
	}

	/**
	 * Returns a receipt for the {@link Payment}.
	 * 
	 * @return
	 */
	public Receipt getReceipt() {
		return new Receipt(paymentDate, order);
	}

	/**
	 * A receipt for an {@link Order} and a payment date.
	 * 
	 * @author Oliver Gierke
	 */
	@Value
	public static class Receipt {

		private final LocalDateTime date;
		private final Order order;
	}
}
