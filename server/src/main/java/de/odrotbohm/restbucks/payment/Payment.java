/*
 * Copyright 2012-2019 the original author or authors.
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
package de.odrotbohm.restbucks.payment;

import de.odrotbohm.restbucks.order.Order;
import de.odrotbohm.restbucks.order.Order.OrderIdentifier;
import de.odrotbohm.restbucks.payment.Payment.PaymentIdentifier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

/**
 * Baseclass for payment implementations.
 *
 * @author Oliver Drotbohm
 */
@Getter
@ToString
@NoArgsConstructor(force = true)
@Table("CREDIT_CARD_PAYMENT")
public abstract class Payment<T extends AggregateRoot<T, PaymentIdentifier>>
		implements AggregateRoot<T, PaymentIdentifier> {

	private final PaymentIdentifier id;
	private final Association<Order, OrderIdentifier> order;
	private final LocalDateTime paymentDate;

	/**
	 * Creates a new {@link Payment} referring to the given {@link Order}.
	 *
	 * @param order must not be {@literal null}.
	 */
	protected Payment(OrderIdentifier orderIdentifier) {

		Assert.notNull(orderIdentifier, "OrderIdentifier must not be null!");

		this.id = new PaymentIdentifier(UUID.randomUUID());
		this.order = Association.forId(orderIdentifier);
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
	 * @author Oliver Drotbohm
	 */
	// TODO: Cannot be a ValueObject as Spring Data REST filters association objects and we'd end up with only one
	// property to serialize, which would cause unwrapping into the EntityModel.
	@Value
	public static class Receipt {

		private final LocalDateTime date;
		private final Association<Order, OrderIdentifier> order;
	}

	public record PaymentIdentifier(UUID id) implements Identifier {}
}
