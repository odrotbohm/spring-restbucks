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
package org.springsource.restbucks.payment.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.Months;
import org.joda.time.Years;
import org.junit.Before;
import org.junit.Test;
import org.springsource.restbucks.payment.CreditCard;
import org.springsource.restbucks.payment.CreditCardNumber;

/**
 * @author Oliver Gierke
 */
public class CreditCardMarshallingTest {

	static final String REFERENCE = "{\"number\":{\"creditCardNumber\":\"1234123412341234\"},\"cardholderName\":\"Oliver Gierke\",\"expiryMonth\":\"P11M\",\"expiryYear\":\"P2013Y\"}";

	ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() {

	}

	@Test
	public void serializesCreditCardWithOutIdAndWithAppropriateMontshAndYears() throws Exception {

		CreditCard creditCard = new CreditCard(new CreditCardNumber("1234123412341234"), "Oliver Gierke", Months.ELEVEN,
				Years.years(2013));
		assertThat(mapper.writeValueAsString(creditCard), is(REFERENCE));
	}

	@Test
	public void deserializesCreditCardWithOutIdAndWithAppropriateMontshAndYears() throws Exception {

		CreditCard creditCard = mapper.readValue(REFERENCE, CreditCard.class);

		assertThat(creditCard.getId(), is(nullValue()));
		assertThat(creditCard.getExpiryMonth(), is(Months.ELEVEN));
		assertThat(creditCard.getExpiryYear(), is(Years.years(2013)));
		assertThat(creditCard.getNumber(), is(new CreditCardNumber("1234123412341234")));
	}
}
