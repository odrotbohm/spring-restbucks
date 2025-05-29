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
package org.springsource.restbucks.dashboard;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springsource.restbucks.order.Order.OrderIdentifier;
import org.springsource.restbucks.order.Order.OrderPaid;

/**
 * Unit tests for {@link Dashboard}.
 *
 * @author Oliver Drotbohm
 */
class DashboardUnitTests {

	Dashboard dashboard = new Dashboard();

	@Test
	void accumulatesValuesOfOrdersPaid() {

		var identifier = new OrderIdentifier(UUID.randomUUID());
		var amount = Money.of(4.20d, "EUR");
		var event = new OrderPaid(identifier, amount);

		dashboard.on(event);

		assertThat(dashboard.statistics()).hasEntrySatisfying("revenue", it -> {
			assertThat(it).isEqualTo(amount);
		});

		dashboard.on(event);

		assertThat(dashboard.statistics()).hasEntrySatisfying("revenue", it -> {
			assertThat(it).isEqualTo(amount.multiply(2));
		});
	}

	@Test
	void registersStatisticsLinkWithRootApiResource() {

		var result = dashboard.process(new RepositoryLinksResource());

		assertThat(result.getLink("statistics")).isPresent();
	}
}
