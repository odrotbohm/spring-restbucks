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

import org.junit.Test;

/**
 * Unit tests for {@link CreditCardNumber}.
 * 
 * @author Oliver Gierke
 */
public class CreditCardNumberUnitTest {

	@Test(expected = IllegalArgumentException.class)
	public void rejectsInvalidLength() {
		new CreditCardNumber("1234");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsLetters() {
		new CreditCardNumber("123412341234123A");
	}

	@Test
	public void createsValidCreditCardNumber() {
		new CreditCardNumber("1234123412341234");
	}
}
