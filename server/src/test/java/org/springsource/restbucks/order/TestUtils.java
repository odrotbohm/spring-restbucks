/*
 * Copyright 2012-2018 the original author or authors.
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
package org.springsource.restbucks.order;

import org.springframework.test.util.ReflectionTestUtils;
import org.springsource.restbucks.order.Order.Status;

/**
 * Utility methods for testing.
 *
 * @author Oliver Gierke
 */
public class TestUtils {

	public static Order createExistingOrder() {

		Order order = new Order();
		ReflectionTestUtils.setField(order, "id", 1L);
		return order;
	}

	public static Order createExistingOrderWithStatus(Status status) {

		Order order = createExistingOrder();
		ReflectionTestUtils.setField(order, "status", status);
		return order;
	}
}
