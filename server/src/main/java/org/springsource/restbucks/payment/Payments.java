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
package org.springsource.restbucks.payment;

import java.util.Optional;

import org.jmolecules.ddd.types.Association;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.OrderIdentifier;

/**
 * Repository interface to manage {@link Payment} instances.
 *
 * @author Oliver Gierke
 */
interface Payments extends CrudRepository<Payment<?>, Long>, PagingAndSortingRepository<Payment<?>, Long> {

	/**
	 * Returns the payment registered for the given {@link Order}.
	 *
	 * @param order
	 * @return
	 */
	default Optional<Payment<?>> findByOrder(OrderIdentifier id) {
		return findByOrder(Association.forId(id));
	}

	Optional<Payment<?>> findByOrder(Association<Order, OrderIdentifier> order);
}
