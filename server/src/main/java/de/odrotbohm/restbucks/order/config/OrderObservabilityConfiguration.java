/*
 * Copyright 2025 the original author or authors.
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
package de.odrotbohm.restbucks.order.config;

import de.odrotbohm.restbucks.order.Order.OrderCreated;
import de.odrotbohm.restbucks.order.Order.OrderLineItemCreated;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.observability.ModulithEventMetricsCustomizer;

/**
 * Additional customizations for counters produced for the events published from the order module.
 *
 * @author Oliver Drotbohm
 */
@Configuration
class OrderObservabilityConfiguration {

	@Bean
	ModulithEventMetricsCustomizer modulithEventMetricsCustomizer() {

		return metrics -> {

			metrics.customize(OrderCreated.class, (event, it) -> {
				it.tags("location", event.location() != null ? event.location().name() : "NONE");
			});

			metrics.customize(OrderLineItemCreated.class, (event, it) -> {

				var lineItem = event.lineItem();

				it.tags("name", lineItem.getName());
				it.tags("milk", lineItem.getMilk().name());
				it.tags("size", lineItem.getSize().name());
			});
		};
	}
}
