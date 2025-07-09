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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.Optional;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Oliver Drotbohm
 */
@Component
class DrinksModelProcessor implements RepresentationModelProcessor<CollectionModel<EntityModel<Drink>>> {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelProcessor#process(org.springframework.hateoas.RepresentationModel)
	 */
	@Override
	public CollectionModel<EntityModel<Drink>> process(CollectionModel<EntityModel<Drink>> model) {

		model.add(linkTo(methodOn(DrinksOptions.class).getOptions(Optional.empty())).withRel("options"));
		return model;
	}
}
