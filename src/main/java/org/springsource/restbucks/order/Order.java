package org.springsource.restbucks.order;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.joda.time.DateTime;
import org.springsource.restbucks.core.AbstractEntity;
import org.springsource.restbucks.core.MonetaryAmount;

@Entity
@Table(name = "RBOrder")
@Getter
@Setter()
public class Order extends AbstractEntity {

	private Location location;
	private Status status;
	private Date orderedDate;

	@OneToMany
	private Set<Item> items = new HashSet<Item>();

	/**
	 * Creates a new {@link Order} for the given {@link Item}s and {@link Location}.
	 * 
	 * @param items must not be {@literal null}.
	 * @param location
	 */
	public Order(Collection<Item> items, Location location) {

		this.location = location == null ? Location.TAKE_AWAY : location;
		this.status = Status.PAYMENT_EXPECTED;
		this.items.addAll(items);
		this.orderedDate = new Date();
	}

	/**
	 * Creates a new {@link Order} containing the given {@link Item}s.
	 * 
	 * @param items must not be {@literal null}.
	 */
	public Order(Item... items) {
		this(Arrays.asList(items), null);
	}

	protected Order() {
		this(new Item[0]);
	}

	/**
	 * Returns the price of the {@link Order} calculated from the contained items.
	 * 
	 * @return will never be {@literal null}.
	 */
	public MonetaryAmount getPrice() {

		MonetaryAmount result = MonetaryAmount.ZERO;

		for (Item item : items) {
			result = result.add(item.getPrice());
		}

		return result;
	}

	public void markPaid() {
		this.status = Status.PAYED;
	}

	public boolean isPaid() {
		return !this.status.equals(Status.PAYMENT_EXPECTED);
	}

	public boolean isReady() {
		return this.status.equals(Status.READY);
	}

	public DateTime getOrderedDate() {
		return orderedDate == null ? null : new DateTime(orderedDate);
	}

	public static enum Status {
		PAYMENT_EXPECTED, PAYED, PREPARING, READY, TAKEN;
	}
}
