/*
 * Copyright 2016-2019 the original author or authors.
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

import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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
		return amount == null ? null : amount.toString();
	}

	@Override
	public MonetaryAmount convertToEntityAttribute(String source) {
		return source == null ? null : Money.parse(source, FORMAT);
	}
}
