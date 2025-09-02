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
package de.odrotbohm.restbucks.payment.web;

import static org.assertj.core.api.Assertions.*;

import de.odrotbohm.restbucks.payment.CreditCard;
import de.odrotbohm.restbucks.payment.CreditCardNumber;
import de.odrotbohm.restbucks.payment.Payment.Receipt;
import de.odrotbohm.restbucks.payment.web.PaymentConfiguration.CreditCardMixin;
import de.odrotbohm.restbucks.payment.web.PaymentConfiguration.ReceiptMixin;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import org.jmolecules.jackson3.JMoleculesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	JsonMapper mapper;

	@BeforeEach
	void setUp() {

		this.mapper = JsonMapper.builder()
				.addModule(new JMoleculesModule())
				.addMixIn(Receipt.class, ReceiptMixin.class)
				.addMixIn(CreditCard.class, CreditCardMixin.class)
				.build();
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
