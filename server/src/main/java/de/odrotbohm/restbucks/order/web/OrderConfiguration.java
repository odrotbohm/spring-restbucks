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
package de.odrotbohm.restbucks.order.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import de.odrotbohm.restbucks.drinks.DrinksOptions;
import de.odrotbohm.restbucks.drinks.Milk;
import de.odrotbohm.restbucks.drinks.Size;
import de.odrotbohm.restbucks.order.LineItem;
import de.odrotbohm.restbucks.order.Location;
import de.odrotbohm.restbucks.order.Order;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import javax.money.MonetaryAmount;

import org.springframework.boot.jackson.JsonMixin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.MediaTypeConfigurationCustomizer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
class OrderConfiguration {

	@Bean
	MediaTypeConfigurationCustomizer<HalFormsConfiguration> orderHalFormsCustomization() {

		Supplier<Link> drinkOptionsLink = () -> linkTo(methodOn(DrinksOptions.class).getOptions(Optional.empty()))
				.withSelfRel()
				.withType(MediaTypes.HAL_JSON_VALUE);

		return config -> config
				.withOptions(LocationAndDrinks.class, "location",
						__ -> HalFormsOptions.inline(Location.values())
								.withSelectedValue(Location.TAKE_AWAY)
								.withMaxItems(1L))
				.withOptions(LocationAndDrinks.class, "drinks",
						__ -> HalFormsOptions.remote(drinkOptionsLink.get()).withMinItems(1L));
	}

	@JsonMixin(Order.class)
	@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
	static abstract class OrderMixin {

		@JsonCreator
		public OrderMixin(Collection<LineItem> lineItems, Location location) {}
	}

	@JsonMixin(LineItem.class)
	static abstract class LineItemMixin {

		@JsonCreator
		public LineItemMixin(String name, int quantity, Milk milk, Size size, MonetaryAmount price) {}
	}
}
