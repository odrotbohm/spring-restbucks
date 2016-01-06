package org.springsource.restbucks.core;

import static org.junit.Assert.*;

import org.javamoney.moneta.Money;
import org.junit.Test;

public class MoneyPersistenceConverterTest {

	@Test
	public void testMoneyPersistenceConverter() {
		MoneyPersistenceConverter converter = new MoneyPersistenceConverter();

		// null-safety
		assertNull(converter.convertToDatabaseColumn(null));
		assertNull(converter.convertToEntityAttribute(null));

		// basic conversion
		assertEquals("EUR 1.23", converter.convertToDatabaseColumn(Money.of(1.23, "EUR")));
		assertEquals(Money.of(1.23, "EUR"), converter.convertToEntityAttribute("EUR 1.23"));

		// expect no rounding
		assertEquals("EUR 1.23456", converter.convertToDatabaseColumn(Money.of(1.23456, "EUR")));

		// expect no formatting
		assertEquals("EUR 123456", converter.convertToDatabaseColumn(Money.of(123456, "EUR")));

		// allow deserialization of formatted values
		assertEquals(Money.of(123456.78, "EUR"), converter.convertToEntityAttribute("EUR 123,456.78"));

		// support negative values
		assertEquals("USD -1.2", converter.convertToDatabaseColumn(Money.of(-1.20, "USD")));
		assertEquals(Money.of(-1.20, "USD"), converter.convertToEntityAttribute("USD -1.2"));
	}

}
