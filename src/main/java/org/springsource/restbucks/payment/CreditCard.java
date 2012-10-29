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

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Years;
import org.springsource.restbucks.core.AbstractEntity;
import org.springsource.restbucks.support.Serializers;

/**
 * Abstraction of a credit card.
 * 
 * @author Oliver Gierke
 */
@Entity
@Getter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditCard extends AbstractEntity {

	private CreditCardNumber number;
	private String cardholderName;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = Serializers.MonthsDeserializer.class)
	private Months expiryMonth;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = Serializers.YearssDeserializer.class)
	private Years expiryYear;

	/**
	 * Returns whether the {@link CreditCard} is valid for the given date.
	 * 
	 * @param date
	 * @return
	 */
	public boolean isValid(LocalDate date) {

		LocalDate reference = new LocalDate(expiryYear.getYears(), expiryMonth.getMonths(), 1);
		return date == null ? false : reference.isBefore(date);
	}
}
