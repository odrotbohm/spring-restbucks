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
package org.springsource.restbucks.payment;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import org.springsource.restbucks.core.AbstractEntity;

/**
 * Abstraction of a credit card.
 * 
 * @author Oliver Gierke
 */
@Entity
@ToString(callSuper = true)
@AllArgsConstructor
public class CreditCard extends AbstractEntity {

	private final @Getter CreditCardNumber number;
	private final @Getter String cardHolderName;

	private Month expiryMonth;
	private Year expiryYear;

	protected CreditCard() {
		this(null, null, null, null);
	}

	/**
	 * Returns whether the {@link CreditCard} is currently valid.
	 * 
	 * @return
	 */
	public boolean isValid() {
		return isValid(LocalDate.now());
	}

	/**
	 * Returns whether the {@link CreditCard} is valid for the given date.
	 * 
	 * @param date
	 * @return
	 */
	public boolean isValid(LocalDate date) {
		return date == null ? false : getExpirationDate().isAfter(date);
	}

	/**
	 * Returns the {@link LocalDate} the {@link CreditCard} expires.
	 * 
	 * @return will never be {@literal null}.
	 */
	public LocalDate getExpirationDate() {
		return LocalDate.of(expiryYear.getValue(), expiryMonth, 1);
	}

	/**
	 * Protected setter to allow binding the expiration date.
	 * 
	 * @param date
	 */
	protected void setExpirationDate(LocalDate date) {

		this.expiryYear = Year.of(date.getYear());
		this.expiryMonth = date.getMonth();
	}
}
