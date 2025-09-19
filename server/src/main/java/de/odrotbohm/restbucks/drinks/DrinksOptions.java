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
package de.odrotbohm.restbucks.drinks;

import static org.springframework.data.domain.Sort.*;

import de.odrotbohm.restbucks.Restbucks;

import java.util.Optional;

import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsPromptedValue;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.TypedEntityLinks;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Oliver Drotbohm
 */
@PrimaryAdapter
@BasePathAwareController
public class DrinksOptions {

	public static final LinkRelation DRINKS_REL = HalLinkRelation.curied(Restbucks.CURIE_NAMESPACE, "drinks");

	private final Drinks drinks;
	private final TypedEntityLinks<Drink> links;

	/**
	 * Creates a new {@link DrinksOptions} for the given {@link Drinks} and {@link EntityLinks}.
	 *
	 * @param drinks must not be {@literal null}.
	 * @param links must not be {@literal null}.
	 */
	DrinksOptions(Drinks drinks, EntityLinks links) {

		Assert.notNull(drinks, "Drinks must not be null!");
		Assert.notNull(links, "EntityLinks must not be null!");

		this.drinks = drinks;
		this.links = links.forType(Drink::getId);
	}

	/**
	 * Expose {@link HalFormsPromptedValue}s for all {@link Drink}s with a name containing the optional request parameter.
	 *
	 * @param q
	 * @return
	 */
	@GetMapping("/drinks/by-name")
	public HttpEntity<?> getOptions(@RequestParam Optional<String> q) {

		var sortByName = sort(Drink.class).by(Drink::getName).ascending();

		var options = q.map(it -> drinks.findByNameContaining(it, sortByName))
				.orElseGet(() -> drinks.findAll(sortByName))
				.map(it -> HalFormsPromptedValue.of(it.getName(), links.linkToItemResource(it).getHref()))
				.toList();

		var model = HalModelBuilder.halModel()
				.embed(options, DRINKS_REL)
				.build();

		return ResponseEntity.ok(model);
	}
}
