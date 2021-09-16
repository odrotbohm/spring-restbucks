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
package org.springsource.restbucks.order.web;

import java.util.Collection;
import java.util.Map;

import javax.money.MonetaryAmount;

import org.springframework.context.annotation.Configuration;
import org.springsource.restbucks.Mixins;
import org.springsource.restbucks.order.LineItem;
import org.springsource.restbucks.order.Location;
import org.springsource.restbucks.order.Milk;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
class OrderConfiguration implements Mixins {

	/*
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.Mixins#getMixins()
	 */
	@Override
	public Map<Class<?>, Class<?>> getMixins() {

		return Map.of( //
				Order.class, OrderMixin.class, //
				LineItem.class, LineItemMixin.class //
		);
	}

	@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
	static abstract class OrderMixin {

		@JsonCreator
		public OrderMixin(Collection<LineItem> lineItems, Location location) {}
	}

	static abstract class LineItemMixin {

		@JsonCreator
		public LineItemMixin(String name, int quantity, Milk milk, Size size, MonetaryAmount price) {}
	}
}
