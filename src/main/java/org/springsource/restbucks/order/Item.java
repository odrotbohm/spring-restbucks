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
