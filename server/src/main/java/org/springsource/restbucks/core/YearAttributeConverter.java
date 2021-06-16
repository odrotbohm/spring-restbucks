/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks.core;

import java.time.Year;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.hibernate.annotations.Immutable;

/**
 * JPA {@link AttributeConverter} implementation to map {@link Year} to an {@link Integer} to avoid Hibernates
 * serializable type handling kick in.
 *
 * @author Oliver Drotbohm
 * @see https://hibernate.atlassian.net/browse/HHH-10558
 */
@Immutable
@Converter(autoApply = true)
public class YearAttributeConverter implements AttributeConverter<Year, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Year year) {
		return year == null ? null : year.getValue();
	}

	@Override
	public Year convertToEntityAttribute(Integer source) {
		return source == null ? null : Year.of(source.intValue());
	}
}
