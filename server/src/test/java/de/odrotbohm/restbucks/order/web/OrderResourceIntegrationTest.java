/*
 * Copyright 2013-2024 the original author or authors.
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
package de.odrotbohm.restbucks.order.web;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import de.odrotbohm.restbucks.AbstractWebIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;

/**
 * Integration test for REST resources exposed by Spring Data REST.
 *
 * @author Oliver Drotbohm
 */
class OrderResourceIntegrationTest extends AbstractWebIntegrationTest {

	@Test
	void exposesOrdersResourceViaRootResource() throws Exception {

		var result = mvc.perform(get("/")); //

		assertThat(result).hasStatus(HttpStatus.OK);
		assertThat(result).contentType().isCompatibleWith(MediaTypes.VND_HAL_JSON);
		assertThat(result).bodyJson().hasPath("$._links.restbucks:orders.href").isNotNull();
	}
}
