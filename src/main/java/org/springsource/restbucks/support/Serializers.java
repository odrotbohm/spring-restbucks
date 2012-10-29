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
package org.springsource.restbucks.support;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.Months;
import org.joda.time.Years;

/**
 * @author Oliver Gierke
 */
public class Serializers {

	public static class MonthsDeserializer extends JsonDeserializer<Months> {

		@Override
		public Months deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			return Months.parseMonths(jp.getText());
		}
	}

	public static class YearssDeserializer extends JsonDeserializer<Years> {

		@Override
		public Years deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			return Years.parseYears(jp.getText());
		}
	}
}
