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
package org.springsource.restbucks.order.web;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import net.minidev.json.parser.JSONParser;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springsource.restbucks.AbstractWebIntegrationTest;

/**
 * Integration test for REST resources exposed by Spring Data REST.
 * 
 * @author Oliver Gierke
 */
public class OrderResourceIntegrationTest extends AbstractWebIntegrationTest {

	@Autowired WebApplicationContext context;

	JSONParser parser;

	@Before
	@Override
	public void setUp() {

		super.setUp();
		parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	}

	@Test
	public void exposesOrdersResourceViaRootResource() throws Exception {

		mvc.perform(get("/")).//
				andExpect(status().isOk()). //
				andExpect(content().contentType(MediaType.APPLICATION_JSON)). //
				andExpect(jsonPath("$links[?(@.rel == 'order')].href", notNullValue()));
	}
}
