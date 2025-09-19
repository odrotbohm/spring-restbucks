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
package de.odrotbohm.restbucks.dashboard;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import de.odrotbohm.restbucks.core.Currencies;
import de.odrotbohm.restbucks.order.Order.OrderPaid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A Spring component to listen to {@link org.springsource.restbucks.order.Order.OrderPaid} events accumulating the
 * values of the orders paid, ultimately exposing the summed-up value as JSON via an HTTP resource advertised on the
 * API's root resource.
 * <p>
 * The component is overloaded on purpose, to — playing devil's advocate here — show, with how few moving parts you can
 * get away, or — to take the opposite position — portrait an application module with sub-optimally arranged internals.
 *
 * @author Oliver Drotbohm
 */
@PrimaryAdapter
@RestController
@RequiredArgsConstructor
class Dashboard implements RepresentationModelProcessor<RepositoryLinksResource> {

	private MonetaryAmount revenue = Money.zero(Currencies.EURO);

	@GetMapping("/statistics")
	Map<String, Object> statistics() {
		return Map.of("revenue", revenue);
	}

	@ApplicationModuleListener
	void on(OrderPaid event) {
		this.revenue = revenue.add(event.total());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	public RepositoryLinksResource process(RepositoryLinksResource model) {
		return model.add(linkTo(methodOn(Dashboard.class).statistics()).withRel("statistics"));
	}
}
