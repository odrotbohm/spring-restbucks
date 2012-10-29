package org.springsource.restbucks.core;

import java.math.BigDecimal;
import java.util.Currency;

import javax.persistence.Embeddable;

import lombok.Data;

import org.springframework.util.Assert;

/**
 * Value object to encapsulate a monetary amount consisting of a currency and a value. Provides basic mathematical
 * abstractions to allow adding up {@link MonetaryAmount}s.
 * 
 * @author Oliver Gierke
 */
@Data
@Embeddable
public class MonetaryAmount {

	public static Currency EURO = Currency.getInstance("EUR");
	public static MonetaryAmount ZERO = new MonetaryAmount();

	private final Currency currency;
	private final BigDecimal value;

	/**
	 * Creates a new {@link MonetaryAmount} instance with the given value and {@link Currency}.
	 * 
	 * @param currency must not be {@literal null}.
	 * @param value
	 */
	public MonetaryAmount(Currency currency, double value) {
		this(currency, new BigDecimal(value));
	}

	/**
	 * Creates a new {@link MonetaryAmount} of the given value and {@link Currency}.
	 * 
	 * @param currency must not be {@literal null}.
	 * @param value must not be {@literal null}.
	 */
	private MonetaryAmount(Currency currency, BigDecimal value) {

		Assert.notNull(currency);
		Assert.notNull(value);

		this.currency = currency;
		this.value = value;
	}

	/**
	 * No-arg constructor to satisfy persistence mechanism.
	 */
	protected MonetaryAmount() {
		this(EURO, BigDecimal.ZERO);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return currency + value.toString();
	}

	/**
	 * Adds the given {@link MonetaryAmount} to the current one. Treats {@literal null} values like
	 * {@link MonetaryAmount#ZERO}.
	 * 
	 * @param other the {@link MonetaryAmount} to add.
	 */
	public MonetaryAmount add(MonetaryAmount other) {

		if (other == null) {
			return this;
		}

		Assert.isTrue(this.currency.equals(other.currency));
		return new MonetaryAmount(this.currency, this.value.add(other.value));
	}
}
