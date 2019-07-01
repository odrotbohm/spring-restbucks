/*
 * Copyright 2016-2018 the original author or authors.
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
package org.springsource.restbucks.core;

import static org.assertj.core.api.Assertions.*;

import org.javamoney.moneta.Money;
import org.junit.Test;

/**
 * Unit tests for {@link MonetaryAmountAttributeConverter}
 *
 * @author Oliver Trosien
 * @author Oliver Gierke
 */
public class MonetaryAmountAttributeConverterTest {

	MonetaryAmountAttributeConverter converter = new MonetaryAmountAttributeConverter();

	/**
	 * @see #51
	 */
	@Test
	public void handlesNullValues() {

		assertThat(converter.convertToDatabaseColumn(null)).isNull();
		assertThat(converter.convertToEntityAttribute(null)).isNull();
	}

	/**
	 * @see #51
	 */
	@Test
	public void handlesSimpleValue() {

		assertThat(converter.convertToDatabaseColumn(Money.of(1.23, "EUR"))).isEqualTo("EUR 1.23");
		assertThat(converter.convertToEntityAttribute("EUR 1.23")).isEqualTo(Money.of(1.23, "EUR"));
	}

	/**
	 * @see #51
	 */
	@Test
	public void handlesNegativeValues() {

		assertThat(converter.convertToDatabaseColumn(Money.of(-1.20, "USD"))).isEqualTo("USD -1.2");
		assertThat(converter.convertToEntityAttribute("USD -1.2")).isEqualTo(Money.of(-1.20, "USD"));
	}

	/**
	 * @see #51
	 */
	@Test
	public void doesNotRoundValues() {
		assertThat(converter.convertToDatabaseColumn(Money.of(1.23456, "EUR"))).isEqualTo("EUR 1.23456");
	}

	/**
	 * @see #51
	 */
	@Test
	public void doesNotFormatLargeValues() {
		assertThat(converter.convertToDatabaseColumn(Money.of(123456, "EUR"))).isEqualTo("EUR 123456");
	}

	/**
	 * @see #51
	 */
	@Test
	public void deserializesFormattedValues() {
		assertThat(converter.convertToEntityAttribute("EUR 123,456.78")).isEqualTo(Money.of(123456.78, "EUR"));
	}
}
