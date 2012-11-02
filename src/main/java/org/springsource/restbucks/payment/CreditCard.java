/*
 * Copyright 2012 the original author or authors.
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

import javax.persistence.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Years;
import org.springsource.restbucks.core.AbstractEntity;

/**
 * Abstraction of a credit card.
 * 
 * @author Oliver Gierke
 */
@Entity
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonAutoDetect(fieldVisibility = Visibility.NONE)
public class CreditCard extends AbstractEntity {

	@Getter
	@JsonUnwrapped
	private CreditCardNumber number;

	@Getter
	@JsonProperty
	private String cardHolderName;

	private Months expiryMonth;
	private Years expiryYear;

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
		return new LocalDate(expiryYear.getYears(), expiryMonth.getMonths(), 1);
	}

	/**
	 * Protected setter to allow binding the expiration date.
	 * 
	 * @param date
	 */
	protected void setExpirationDate(LocalDate date) {

		this.expiryYear = Years.years(date.getYear());
		this.expiryMonth = Months.months(date.getMonthOfYear());
	}
}
