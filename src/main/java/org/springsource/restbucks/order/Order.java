package org.springsource.restbucks.order;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.joda.time.DateTime;
import org.springsource.restbucks.core.AbstractEntity;
import org.springsource.restbucks.core.MonetaryAmount;

@Entity
@Getter
@Setter
@ToString(exclude = "items")
@Table(name = "RBOrder")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, isGetterVisibility = Visibility.NONE)
public class Order extends AbstractEntity {

	private Location location;
	private Status status;
	private Date orderedDate;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
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

	public Order() {
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

	/**
	 * Marks the {@link Order} as payed.
	 */
	public void markPaid() {

		if (isPaid()) {
			throw new IllegalStateException("Already paid order cannot be paid again!");
		}

		this.status = Status.PAYED;
	}

	/**
	 * Marks the {@link Order} as in preparation.
	 */
	public void markInPreparation() {

		if (this.status != Status.PAYED) {
			throw new IllegalStateException(String.format("Order must be in state payed to start preparation! "
					+ "Current status: %s", this.status));
		}

		this.status = Status.PREPARING;
	}

	/**
	 * Marks the {@link Order} as prepared.
	 */
	public void markPrepared() {

		if (this.status != Status.PREPARING) {
			throw new IllegalStateException(String.format("Cannot mark Order prepared that is currently not "
					+ "preparing! Current status: %s.", this.status));
		}

		this.status = Status.READY;
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
	 * Returns the date the {@link Order} was placed.
	 * 
	 * @return
	 */
	public DateTime getOrderedDate() {
		return orderedDate == null ? null : new DateTime(orderedDate);
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
		PAYED,

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
}
