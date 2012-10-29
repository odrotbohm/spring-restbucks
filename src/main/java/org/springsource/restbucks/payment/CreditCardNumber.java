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

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.util.Assert;

/**
 * @author Oliver Gierke
 */
@Data
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditCardNumber {

	private static final String regex = "[0-9]{16}";

	@Column(unique = true)
	private String creditCardNumber;

	/**
	 * Creates a new {@link CreditCardNumber}.
	 * 
	 * @param creditCardNumber must not be {@literal null} and be a 16 digit numerical only String.
	 */
	public CreditCardNumber(String creditCardNumber) {

		Assert.notNull(creditCardNumber);

		if (!creditCardNumber.matches(regex)) {
			throw new IllegalArgumentException();
		}

		this.creditCardNumber = creditCardNumber;
	}
}
