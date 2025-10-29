/*
 * Copyright 2012-2019 the original author or authors.
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
package de.odrotbohm.restbucks.order;

import de.odrotbohm.restbucks.order.Order.OrderIdentifier;
import de.odrotbohm.restbucks.order.Order.Status;

import java.util.List;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository to manage {@link Order} instances.
 *
 * @author Oliver Drotbohm
 */
@RepositoryRestResource(excerptProjection = OrderProjection.class)
public interface Orders extends CrudRepository<Order, OrderIdentifier>,
		AssociationResolver<Order, OrderIdentifier>,
		PagingAndSortingRepository<Order, OrderIdentifier> {

	/**
	 * Returns all {@link Order}s with the given {@link Status}.
	 *
	 * @param status must not be {@literal null}.
	 * @return
	 */
	List<Order> findByStatus(@Param("status") Status status);

	/**
	 * Marks the given {@link Order} as paid.
	 *
	 * @param order must not be {@literal null}.
	 * @return
	 */
	@Transactional
	default Order markPaid(Order order) {
		return save(order.markPaid());
	}

	/**
	 * Marks the order with the given {@link OrderIdentifier} as in preparation.
	 *
	 * @param id must not be {@literal null}.
	 * @return
	 */
	@Transactional
	default Order markInPreparation(OrderIdentifier id) {

		var order = findById(id) //
				.orElseThrow(() -> new IllegalStateException(String.format("No order found for identifier %s!", id)));

		return save(order.markInPreparation());
	}

	/**
	 * Marks the given Order as prepared.
	 *
	 * @param order must not be {@literal null}.
	 * @return
	 */
	@Transactional
	default Order markPrepared(Order order) {
		return save(order.markPrepared());
	}

	/**
	 * Marks the given {@link Order} as taken.
	 *
	 * @param order must not be {@literal null}.
	 * @return
	 */
	@Transactional
	default Order markTaken(Order order) {
		return save(order.markTaken());
	}
}
