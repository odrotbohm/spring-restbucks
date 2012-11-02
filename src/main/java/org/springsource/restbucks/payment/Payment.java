package org.springsource.restbucks.payment;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.joda.time.DateTime;
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
@NoArgsConstructor
public abstract class Payment extends AbstractEntity {

	@OneToOne(cascade = CascadeType.MERGE)
	private Order order;
	private Date paymentDate;

	/**
	 * Creates a new {@link Payment} referring to the given {@link Order}.
	 * 
	 * @param order must not be {@literal null}.
	 */
	public Payment(Order order) {

		Assert.notNull(order);
		this.order = order;
		this.paymentDate = new Date();
	}

	/**
	 * Returns a receipt for the {@link Payment}.
	 * 
	 * @return
	 */
	public Receipt getReceipt() {
		return new Receipt(order, new DateTime(paymentDate));
	}

	/**
	 * A receipt for an {@link Order} and a payment date.
	 * 
	 * @author Oliver Gierke
	 */
	@Data
	public static class Receipt {

		private final Order order;
		private final DateTime date;
	}
}
