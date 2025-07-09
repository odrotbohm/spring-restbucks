/*
 * Copyright 2012-2021 the original author or authors.
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

import de.odrotbohm.restbucks.drinks.Drink;
import de.odrotbohm.restbucks.order.Order.OrderIdentifier;
import jakarta.persistence.Column;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * An order.
 *
 * @author Oliver Gierke
 */
@Getter
@ToString(exclude = "lineItems")
@Table(name = "RBOrder")
public class Order extends AbstractAggregateRoot<Order> implements AggregateRoot<Order, OrderIdentifier> {

	private final OrderIdentifier id;
	private final Location location;
	private final LocalDateTime orderedDate;
	private Status status;
	private @Version Long version;

	@OrderColumn //
	@Column(unique = true) //
	private final List<LineItem> lineItems = new ArrayList<>();

	/**
	 * Creates a new {@link Order} for the given {@link LineItem}s and {@link Location}.
	 *
	 * @param lineItems must not be {@literal null}.
	 * @param location
	 */
	public Order(Collection<LineItem> lineItems, Location location) {

		this.id = new OrderIdentifier(UUID.randomUUID());
		this.location = location == null ? Location.TAKE_AWAY : location;
		this.status = Status.PAYMENT_EXPECTED;
		this.lineItems.addAll(lineItems);
		this.orderedDate = LocalDateTime.now();
	}

	/**
	 * Creates a new {@link Order} containing the given {@link LineItem}s.
	 *
	 * @param items must not be {@literal null}.
	 */
	public Order(LineItem... items) {
		this(List.of(items), null);
	}

	Order() {
		this(new LineItem[0]);
	}

	/**
	 * Returns the price of the {@link Order} calculated from the contained items.
	 *
	 * @return will never be {@literal null}.
	 */
	public MonetaryAmount getPrice() {

		return lineItems.stream().//
				map(LineItem::getPrice).//
				reduce(MonetaryAmount::add).orElse(Money.of(0.0, "EUR"));
	}

	public Order add(Drink drink) {

		lineItems.stream()
				.filter(it -> it.refersTo(drink))
				.findFirst()
				.map(it -> it.increaseAmount())

				.orElseGet(() -> {

					LineItem item = new LineItem(drink);
					this.lineItems.add(item);
					return item;
				});

		return this;
	}

	/**
	 * Marks the {@link Order} as payed.
	 */
	Order markPaid() {

		if (isPaid()) {
			throw new IllegalStateException("Already paid order cannot be paid again!");
		}

		this.status = Status.PAID;

		registerEvent(new OrderPaid(id, getPrice()));

		return this;
	}

	/**
	 * Marks the {@link Order} as in preparation.
	 */
	Order markInPreparation() {

		if (this.status != Status.PAID) {
			throw new IllegalStateException(
					String.format("Order must be in state payed to start preparation! Current status: %s", this.status));
		}

		this.status = Status.PREPARING;

		return this;
	}

	/**
	 * Marks the {@link Order} as prepared.
	 */
	Order markPrepared() {

		if (this.status != Status.PREPARING) {
			throw new IllegalStateException(String
					.format("Cannot mark Order prepared that is currently not preparing! Current status: %s.", this.status));
		}

		this.status = Status.READY;

		return this;
	}

	Order markTaken() {

		if (this.status != Status.READY) {
			throw new IllegalStateException(
					String.format("Cannot mark Order taken that is currently not paid! Current status: %s.", this.status));
		}

		this.status = Status.TAKEN;

		return this;
	}

	/**
	 * Returns whether the {@link Order} has been paid already.
	 *
	 * @return
	 */
	public boolean isPaid() {
		return !this.status.equals(Status.PAYMENT_EXPECTED);
	}

	/**
	 * Returns if the {@link Order} is ready to be taken.
	 *
	 * @return
	 */
	public boolean isReady() {
		return this.status.equals(Status.READY);
	}

	public boolean isTaken() {
		return this.status.equals(Status.TAKEN);
	}

	/**
	 * Enumeration for all the statuses an {@link Order} can be in.
	 *
	 * @author Oliver Gierke
	 */
	public static enum Status {

		/**
		 * Placed, but not payed yet. Still changeable.
		 */
		PAYMENT_EXPECTED,

		/**
		 * {@link Order} was payed. No changes allowed to it anymore.
		 */
		PAID,

		/**
		 * The {@link Order} is currently processed.
		 */
		PREPARING,

		/**
		 * The {@link Order} is ready to be picked up by the customer.
		 */
		READY,

		/**
		 * The {@link Order} was completed.
		 */
		TAKEN;
	}

	public record OrderIdentifier(UUID id) implements Identifier {}

	/**
	 * Event to be thrown when an {@link Order} has been paid.
	 *
	 * @author Oliver Gierke
	 * @author Stéphane Nicoll
	 */
	public record OrderPaid(
			OrderIdentifier orderIdentifier,
			MonetaryAmount total) implements DomainEvent {}
}
