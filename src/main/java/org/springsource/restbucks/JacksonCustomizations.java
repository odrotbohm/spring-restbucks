/*
 * Copyright 2015 the original author or authors.
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
package org.springsource.restbucks;

import java.io.IOException;
import java.util.Locale;

import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.payment.CreditCard;
import org.springsource.restbucks.payment.CreditCardNumber;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Configures custom serialization and deserialization of {@link Money} instances
 *
 * @author Oliver Gierke
 */
@Configuration
class JacksonCustomizations {

	public @Bean Module moneyModule() {
		return new MoneyModule();
	}

	public @Bean Module restbucksModule() {
		return new RestbucksModule();
	}

	@SuppressWarnings("serial")
	static class RestbucksModule extends SimpleModule {

		public RestbucksModule() {

			setMixInAnnotation(Order.class, RestbucksModule.OrderMixin.class);
			setMixInAnnotation(CreditCard.class, CreditCardMixin.class);
			setMixInAnnotation(CreditCardNumber.class, CreditCardNumberMixin.class);
		}

		@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
				isGetterVisibility = JsonAutoDetect.Visibility.NONE)
		static abstract class OrderMixin {}

		@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
		static abstract class CreditCardMixin {

			abstract @JsonUnwrapped CreditCardNumber getNumber();
		}

		@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
		static abstract class CreditCardNumberMixin {}
	}

	@SuppressWarnings("serial")
	static class MoneyModule extends SimpleModule {

		private static final MonetaryAmountFormat FORMAT = MonetaryFormats.getAmountFormat(Locale.ROOT);

		public MoneyModule() {
			addSerializer(Money.class, new MoneySerializer());
			addValueInstantiator(Money.class, new MoneyInstantiator());
		}

		static class MoneySerializer extends ToStringSerializer {

			@Override
			public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
					JsonGenerationException {
				jgen.writeString(FORMAT.format((Money) value));
			}
		}

		static class MoneyInstantiator extends ValueInstantiator {

			@Override
			public String getValueTypeDesc() {
				return Money.class.toString();
			}

			@Override
			public boolean canCreateFromString() {
				return true;
			}

			@Override
			public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
				return Money.parse(value, FORMAT);
			}
		}
	}
}
