/*
 * Copyright 2015-2019 the original author or authors.
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.rest.webmvc.json.JsonSchema.JsonSchemaProperty;
import org.springframework.data.rest.webmvc.json.JsonSchemaPropertyCustomizer;
import org.springframework.data.util.TypeInformation;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springsource.restbucks.drinks.DrinksOptions;
import org.springsource.restbucks.order.Location;
import org.springsource.restbucks.order.web.LocationAndDrinks;
import org.springsource.restbucks.payment.CreditCardNumber;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Configures custom serialization and deserialization of {@link Money} instances
 *
 * @author Oliver Gierke
 */
@Configuration(proxyBeanMethods = false)
class JacksonCustomizations {

	public @Bean HalFormsConfiguration halFormsConfiguration() {

		return new HalFormsConfiguration()
				.withPattern(CreditCardNumber.class, CreditCardNumber.REGEX)
				.withOptions(LocationAndDrinks.class, "location",
						it -> HalFormsOptions.inline(Location.values()).withSelectedValue(Location.TAKE_AWAY).withMaxItems(1L))
				.withOptions(LocationAndDrinks.class, "drinks",
						it -> HalFormsOptions.remote(linkTo(methodOn(DrinksOptions.class).getOptions(Optional.empty())).toString())
								.withMinItems(1L));
	}

	public @Bean Module moneyModule() {
		return new MoneyModule();
	}

	public @Bean Module restbucksModule(List<Mixins> mixins) {

		Map<Class<?>, Class<?>> annotations = mixins.stream().map(Mixins::getMixins)
				.reduce(new HashMap<>(), (left, right) -> {
					left.putAll(right);
					return left;
				});

		return new RestbucksModule(annotations);
	}

	@SuppressWarnings("serial")
	static class RestbucksModule extends SimpleModule {

		public RestbucksModule(Map<Class<?>, Class<?>> mixins) {
			mixins.entrySet().forEach(it -> setMixInAnnotation(it.getKey(), it.getValue()));
		}
	}

	@SuppressWarnings("serial")
	static class MoneyModule extends SimpleModule {

		public MoneyModule() {

			addSerializer(MonetaryAmount.class, new MonetaryAmountSerializer());
			addValueInstantiator(MonetaryAmount.class, new MoneyInstantiator());
		}

		/**
		 * A dedicated serializer to render {@link MonetaryAmount} instances as formatted {@link String}. Also implements
		 * {@link JsonSchemaPropertyCustomizer} to expose the different rendering to the schema exposed by Spring Data REST.
		 *
		 * @author Oliver Gierke
		 */
		static class MonetaryAmountSerializer extends StdSerializer<MonetaryAmount>
				implements JsonSchemaPropertyCustomizer {

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

			public MonetaryAmountSerializer() {
				super(MonetaryAmount.class);
			}

			/*
			 * (non-Javadoc)
			 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
			 */
			@Override
			public void serialize(MonetaryAmount value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

				if (value != null) {
					jgen.writeString(MonetaryFormats.getAmountFormat(LocaleContextHolder.getLocale()).format(value));
				} else {
					jgen.writeNull();
				}
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
				return MonetaryAmount.class.toString();
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
			public Object createFromString(DeserializationContext context, String value) throws IOException {
				return Money.parse(value, MonetaryFormats.getAmountFormat(LocaleContextHolder.getLocale()));
			}
		}
	}
}
