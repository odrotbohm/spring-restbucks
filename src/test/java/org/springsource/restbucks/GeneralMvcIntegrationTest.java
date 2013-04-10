/*
 * Copyright 2012-2013 the original author or authors.
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
package org.springsource.restbucks;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Sample integration test to test various general Spring MVC related stuff.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class GeneralMvcIntegrationTest {

	@Configuration
	@EnableWebMvc
	static class WebTestConfiguration {

		@Bean
		public LinkHeaderController controller() {
			return new LinkHeaderController();
		}

	}

	@Autowired
	WebApplicationContext wac;

	MockMvc mvc;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * @see https://github.com/SpringSource/spring-hateoas/issues/54
	 */
	@Test
	public void handsLinkHeadersIntoControllerMethod() throws Exception {

		Link link = new Link("/something");

		mvc.perform(get("/links").header("Link", link.toString())). //
				andExpect(header().string("Link", link.toString())). //
				andExpect(content().string("Result"));
	}

	@Controller
	static class LinkHeaderController {

		@RequestMapping("/links")
		HttpEntity<?> linkHeaders(@RequestHeader("Link") Links links) {

			HttpHeaders headers = new HttpHeaders();
			headers.add("Link", links.toString());

			return new ResponseEntity<String>("Result", headers, HttpStatus.OK);
		}
	}
}
