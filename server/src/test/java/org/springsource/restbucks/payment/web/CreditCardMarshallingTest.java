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
package org.springsource.restbucks.payment.web;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import org.junit.Before;
import org.junit.Test;
import org.springsource.restbucks.JacksonTestUtils;
import org.springsource.restbucks.payment.CreditCard;
import org.springsource.restbucks.payment.CreditCardNumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

/**
 * Integration tests for marshaling of {@link CreditCard}.
 * 
 * @author Oliver Gierke
 */
public class CreditCardMarshallingTest {

	static final String REFERENCE = "{\"number\":\"1234123412341234\",\"cardHolderName\":\"Oliver Gierke\",\"expirationDate\":[2013,11,1]}";

	ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() {

		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new ParameterNamesModule());
		mapper.registerModules(JacksonTestUtils.getModules());
	}

	@Test
	public void serializesCreditCardWithOutIdAndWithAppropriateMontshAndYears() throws Exception {

		CreditCard creditCard = new CreditCard(new CreditCardNumber("1234123412341234"), "Oliver Gierke", Month.NOVEMBER,
				Year.of(2013));

		String result = mapper.writeValueAsString(creditCard);

		assertThat(JsonPath.<String> read(result, "$.number")).isEqualTo("1234123412341234");
		assertThat(JsonPath.<String> read(result, "$.cardHolderName")).isEqualTo("Oliver Gierke");
		assertThat(JsonPath.<String> read(result, "$.expirationDate")).isEqualTo("2013-11-01");

		Configuration configuration = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);

		Object read = JsonPath.compile("$.valid").read(result, configuration);
		assertThat(read).isNull();
	}

	@Test
	public void deserializesCreditCardWithOutIdAndWithAppropriateMontshAndYears() throws Exception {

		CreditCard creditCard = mapper.readValue(REFERENCE, CreditCard.class);

		assertThat(creditCard.getId()).isNull();
		assertThat(creditCard.getExpirationDate()).isEqualTo(LocalDate.of(2013, 11, 1));
		assertThat(creditCard.getNumber()).isNotNull();
	}
}
