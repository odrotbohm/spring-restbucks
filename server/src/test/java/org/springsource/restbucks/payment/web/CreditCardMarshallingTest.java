/*
 * Copyright 2012-2021 the original author or authors.
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

import org.jmolecules.jackson.JMoleculesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springsource.restbucks.payment.CreditCard;
import org.springsource.restbucks.payment.CreditCardNumber;
import org.springsource.restbucks.payment.Payment.Receipt;
import org.springsource.restbucks.payment.web.PaymentConfiguration.CreditCardMixin;
import org.springsource.restbucks.payment.web.PaymentConfiguration.ReceiptMixin;

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
class CreditCardMarshallingTest {

	static final String REFERENCE = "{\"number\":\"1234123412341234\",\"cardHolderName\":\"Oliver Gierke\",\"expirationDate\":[2013,11,1]}";

	ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setUp() {

		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new ParameterNamesModule());
		mapper.registerModule(new JMoleculesModule());
		mapper.addMixIn(Receipt.class, ReceiptMixin.class);
		mapper.addMixIn(CreditCard.class, CreditCardMixin.class);
	}

	@Test
	void serializesCreditCardWithOutIdAndWithAppropriateMontshAndYears() throws Exception {

		CreditCard creditCard = new CreditCard(CreditCardNumber.of("1234123412341234"), "Oliver Gierke", Month.NOVEMBER,
				Year.of(2013));

		String result = mapper.writeValueAsString(creditCard);

		assertThat(JsonPath.<String> read(result, "$.number")).isEqualTo("1234123412341234");
		assertThat(JsonPath.<String> read(result, "$.cardHolderName")).isEqualTo("Oliver Gierke");
		assertThat(JsonPath.<String> read(result, "$.expirationDate")).isEqualTo("2013-11-01");

		Configuration configuration = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);

		assertThat(JsonPath.compile("$.valid").<Boolean> read(result, configuration)).isNull();
	}

	@Test
	void deserializesCreditCardWithOutIdAndWithAppropriateMontshAndYears() throws Exception {

		CreditCard creditCard = mapper.readValue(REFERENCE, CreditCard.class);

		assertThat(creditCard.getId()).isEqualTo(creditCard.getNumber());
		assertThat(creditCard.getExpirationDate()).isEqualTo(LocalDate.of(2013, 11, 1));
		assertThat(creditCard.getNumber()).isNotNull();
	}
}
