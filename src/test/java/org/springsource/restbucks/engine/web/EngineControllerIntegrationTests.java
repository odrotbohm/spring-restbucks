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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springsource.restbucks.AbstractWebIntegrationTest;

/**
 * Integration tests for {@link EngineController}.
 * 
 * @author Oliver Gierke
 */
public class EngineControllerIntegrationTests extends AbstractWebIntegrationTest {

	@Test
	public void customControllerReturnsDefaultMediaType() throws Exception {

		MockHttpServletResponse response = mvc.perform(get("/")).//
				andExpect(linkWithRelIsPresent(EngineController.ENGINE_REL)). //
				andReturn().getResponse();

		Link link = links.findLinkWithRel(EngineController.ENGINE_REL, response.getContentAsString());

		mvc.perform(get(link.getHref())). //
				andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
	}
}
