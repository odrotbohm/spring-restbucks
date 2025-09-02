/*
 * Copyright 2016-2021 the original author or authors.
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

import de.odrotbohm.restbucks.JacksonTestUtils;
import de.odrotbohm.restbucks.core.Currencies;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Unit tests for serialization and deserialization of {@link MonetaryAmount} values.
 *
 * @author Oliver Gierke
 */
class MoneySerializationTest {

	JsonMapper mapper;

	@BeforeEach
	void setUp() {

		this.mapper = JsonMapper.builder()
				.addModules(JacksonTestUtils.getModules())
				.build();

		LocaleContextHolder.setLocale(Locale.ROOT);
	}

	/**
	 * @see #50
	 */
	@Test
	void serializesMonetaryAmount() throws Exception {
		assertThat(mapper.writeValueAsString(Money.of(4.20, Currencies.EURO))).isEqualTo("\"EUR 4.20\"");
	}

	/**
	 * @see #50
	 */
	@Test
	void deserializesMonetaryAmount() throws Exception {
		assertThat(mapper.readValue("\"EUR 4.20\"", MonetaryAmount.class)).isEqualTo(Money.of(4.20, Currencies.EURO));
	}
}
