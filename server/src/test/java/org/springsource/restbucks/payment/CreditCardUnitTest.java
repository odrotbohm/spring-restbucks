/*
 * Copyright 2012-2018 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import org.junit.Test;

/**
 * Unit tests for {@link CreditCard}.
 * 
 * @author Oliver Gierke
 */
public class CreditCardUnitTest {

	static final CreditCardNumber NUMBER = new CreditCardNumber("1234123412341234");

	@Test
	public void discoversExpiredCreditCard() {

		CreditCard creditCard = new CreditCard(NUMBER, "Oliver Gierke", Month.DECEMBER, Year.of(2016));

		assertThat(creditCard.isValid(LocalDate.of(2016, 1, 1))).isTrue();
		assertThat(creditCard.isValid(LocalDate.of(2016, 12, 1))).isFalse();
	}
}
