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
package de.odrotbohm.restbucks.core;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.Money;

/**
 * JPA {@link AttributeConverter} to serialize {@link MonetaryAmount} instances into a {@link String}. Auto-applied to
 * all entity properties of type {@link MonetaryAmount}.
 *
 * @author Oliver Trosien
 * @author Oliver Gierke
 */
@Converter(autoApply = true)
public class MonetaryAmountAttributeConverter implements AttributeConverter<MonetaryAmount, String> {

	private static final MonetaryAmountFormat FORMAT = MonetaryFormats.getAmountFormat(Locale.ROOT);

	@Override
	public String convertToDatabaseColumn(MonetaryAmount amount) {

		return amount == null ? null
				: String.format("%s %s", amount.getCurrency().toString(), amount.getNumber().toString());
	}

	@Override
	public Money convertToEntityAttribute(String source) {

		if (source == null) {
			return null;
		}

		try {
			return Money.parse(source);
		} catch (RuntimeException e) {

			try {
				return Money.parse(source, FORMAT);
			} catch (RuntimeException inner) {

				// Propagate the original exception in case the fallback fails
				throw e;
			}
		}
	}
}
