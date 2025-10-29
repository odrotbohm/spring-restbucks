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
package de.odrotbohm.restbucks.payment;

import lombok.Value;

import java.util.regex.Pattern;

import org.jmolecules.ddd.types.Identifier;
import org.jmolecules.ddd.types.ValueObject;

/**
 * Value object to represent a {@link CreditCardNumber}.
 *
 * @author Oliver Drotbohm
 */
@Value(staticConstructor = "of")
public class CreditCardNumber implements Identifier, ValueObject {

	public static final String REGEX = "[0-9]{16}";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private final String number;

	/**
	 * Creates a new {@link CreditCardNumber}.
	 *
	 * @param number must not be {@literal null} and be a 16 digit numerical only String.
	 */
	private CreditCardNumber(String number) {

		if (!isValid(number)) {
			throw new IllegalArgumentException(String.format("Invalid credit card number %s!", number));
		}

		this.number = number;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return number;
	}

	/**
	 * Returns whether the given {@link String} is a valid {@link CreditCardNumber}.
	 *
	 * @param number
	 * @return
	 */
	public static boolean isValid(String number) {
		return number == null ? false : PATTERN.matcher(number).matches();
	}
}
