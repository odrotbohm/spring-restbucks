/*
 * Copyright 2013-2019 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springsource.restbucks.AbstractWebIntegrationTest;

/**
 * Integration test for REST resources exposed by Spring Data REST.
 *
 * @author Oliver Gierke
 */
class OrderResourceIntegrationTest extends AbstractWebIntegrationTest {

	@Test
	void exposesOrdersResourceViaRootResource() throws Exception {

		mvc.perform(get("/")) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON)) //
				.andExpect(jsonPath("$._links.restbucks:orders.href", notNullValue()));
	}
}
