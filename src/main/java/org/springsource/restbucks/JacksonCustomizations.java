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
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.json.JsonSchema.JsonSchemaProperty;
import org.springframework.data.rest.webmvc.json.JsonSchemaPropertyCustomizer;
import org.springframework.data.util.TypeInformation;
import org.springsource.restbucks.order.LineItem;
import org.springsource.restbucks.order.Location;
import org.springsource.restbucks.order.Milk;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Size;
import org.springsource.restbucks.payment.CreditCard;
import org.springsource.restbucks.payment.CreditCardNumber;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
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
			setMixInAnnotation(LineItem.class, LineItemMixin.class);
			setMixInAnnotation(CreditCard.class, CreditCardMixin.class);
			setMixInAnnotation(CreditCardNumber.class, CreditCardNumberMixin.class);
		}

		@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
		static abstract class OrderMixin {

			@JsonCreator
			public OrderMixin(Collection<LineItem> lineItems, Location location) {}
		}

		static abstract class LineItemMixin {

			@JsonCreator
			public LineItemMixin(String name, int amount, Milk milk, Size size, Money price) {}
		}

		@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
		static abstract class CreditCardMixin {

			abstract @JsonUnwrapped CreditCardNumber getNumber();
		}

		@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
		static abstract class CreditCardNumberMixin {}
	}

	@SuppressWarnings("serial")
	static class MoneyModule extends SimpleModule {

		private static final MonetaryAmountFormat FORMAT = MonetaryFormats.getAmountFormat(Locale.US);

		public MoneyModule() {

			addSerializer(MonetaryAmount.class, new MonetaryAmountSerializer());
			addValueInstantiator(Money.class, new MoneyInstantiator());
		}

		/**
		 * A dedicated serializer to render {@link MonetaryAmount} instances as formatted {@link String}. Also implements
		 * {@link JsonSchemaPropertyCustomizer} to expose the different rendering to the schema exposed by Spring Data REST.
		 *
		 * @author Oliver Gierke
		 */
		static class MonetaryAmountSerializer extends ToStringSerializer implements JsonSchemaPropertyCustomizer {

			private static final Pattern MONEY_PATTERN;

			static {

				StringBuilder builder = new StringBuilder();

				builder.append("(?=.)^"); // Start
				builder.append("[A-Z]{3}?"); // 3-digit currency code
				builder.append("\\s"); // single whitespace character
				builder.append("(([1-9][0-9]{0,2}(,[0-9]{3})*)|[0-9]+)?"); // digits with optional grouping by "," every 3)
				builder.append("(\\.[0-9]{1,2})?$"); // End with a dot and two digits

				MONEY_PATTERN = Pattern.compile(builder.toString());
			}

			/*
			 * (non-Javadoc)
			 * @see com.fasterxml.jackson.databind.ser.std.ToStringSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
			 */
			@Override
			public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
					throws IOException, JsonGenerationException {
				jgen.writeString(FORMAT.format((MonetaryAmount) value));
			}

			/*
			 * (non-Javadoc)
			 * @see org.springframework.data.rest.webmvc.json.JsonSchemaPropertyCustomizer#customize(org.springframework.data.rest.webmvc.json.JsonSchema.JsonSchemaProperty, org.springframework.data.util.TypeInformation)
			 */
			@Override
			public JsonSchemaProperty customize(JsonSchemaProperty property, TypeInformation<?> type) {
				return property.withType(String.class).withPattern(MONEY_PATTERN);
			}
		}

		static class MoneyInstantiator extends ValueInstantiator {

			/*
			 * (non-Javadoc)
			 * @see com.fasterxml.jackson.databind.deser.ValueInstantiator#getValueTypeDesc()
			 */
			@Override
			public String getValueTypeDesc() {
				return Money.class.toString();
			}

			/*
			 * (non-Javadoc)
			 * @see com.fasterxml.jackson.databind.deser.ValueInstantiator#canCreateFromString()
			 */
			@Override
			public boolean canCreateFromString() {
				return true;
			}

			/*
			 * (non-Javadoc)
			 * @see com.fasterxml.jackson.databind.deser.ValueInstantiator#createFromString(com.fasterxml.jackson.databind.DeserializationContext, java.lang.String)
			 */
			@Override
			public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
				return Money.parse(value, FORMAT);
			}
		}
	}
}
