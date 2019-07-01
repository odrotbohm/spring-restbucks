/*
 * Copyright 2014 the original author or authors.
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
package org.springsource.restbucks.order.web;

import java.time.LocalDateTime;

import org.springframework.data.rest.core.config.Projection;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.order.Order.Status;

/**
 * Projection interface to render {@link Order} summaries.
 * 
 * @author Oliver Gierke
 */
@Projection(name = "summary", types = Order.class)
public interface OrderProjection {

	/**
	 * @see Order#getOrderedDate()
	 * @return
	 */
	LocalDateTime getOrderedDate();

	/**
	 * @see Order#getStatus()
	 * @return
	 */
	Status getStatus();
}
