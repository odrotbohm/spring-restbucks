/*
 * Copyright 2013 the original author or authors.
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
package org.springsource.restbucks.engine.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springsource.restbucks.engine.InProgressAware;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.OrderRepository;

/**
 * Custom controller to show how to expose a custom resource into the root resource exposed by Spring Data REST.
 * 
 * @author Oliver Gierke
 */
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class EngineController implements ResourceProcessor<RepositoryLinksResource> {

	public static final String ENGINE_REL = "engine";
	public static final String PAGES_REL = "pages";

	private final @NonNull InProgressAware processor;
	private final @NonNull OrderRepository repository;

	/**
	 * Exposes all {@link Order}s currently in preparation.
	 * 
	 * @return
	 */
	@RequestMapping("/engine")
	HttpEntity<Resources<Resource<Order>>> showOrdersInProgress() {

		Resources<Resource<Order>> orderResources = Resources.wrap(processor.getOrders());
		orderResources.add(linkTo(methodOn(EngineController.class).showOrdersInProgress()).withSelfRel());

		return new ResponseEntity<>(orderResources, HttpStatus.OK);
	}

	/**
	 * An additional resource exposing paged {@link Order}s to show the usage of both {@link Pageable} and
	 * {@link PagedResourcesAssembler} in controller methods. Both arguments are resolved through infrastructure
	 * components registered through {@link EnableSpringDataWebSupport} on
	 * {@link org.springsource.restbucks.Restbucks.WebConfiguration}.
	 * 
	 * @param pageable the {@link Pageable} derived from the request.
	 * @param assembler the {@link PagedResourcesAssembler} to easily build {@link PagedResources} instances from
	 *          {@link Page}s.
	 * @return
	 */
	@RequestMapping("/pages")
	HttpEntity<PagedResources<Resource<Order>>> foo(Pageable pageable, PagedResourcesAssembler<Order> assembler) {

		Page<Order> orders = repository.findAll(pageable);
		return new ResponseEntity<>(assembler.toResource(orders), HttpStatus.OK);
	}

	/**
	 * Exposes the {@link EngineController} to the root resource exposed by Spring Data REST.
	 * 
	 * @see org.springframework.hateoas.ResourceProcessor#process(org.springframework.hateoas.ResourceSupport)
	 */
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {

		resource.add(linkTo(methodOn(EngineController.class).showOrdersInProgress()).withRel(ENGINE_REL));
		resource.add(linkTo(EngineController.class).slash("pages").withRel(PAGES_REL));

		return resource;
	}
}
