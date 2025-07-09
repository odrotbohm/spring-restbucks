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
package de.odrotbohm.restbucks;

import lombok.SneakyThrows;

import java.util.Locale;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class to derive concrete web test classes from.
 *
 * @author Oliver Gierke
 */
@SpringBootTest
public abstract class AbstractWebIntegrationTest {

	@Autowired WebApplicationContext context;
	@Autowired LinkDiscoverers links;

	protected MockMvcTester mvc;

	@BeforeEach
	void setUp() {

		this.mvc = MockMvcTester.from(context, builder ->//
				builder.defaultRequest(MockMvcRequestBuilders.get("/").locale(Locale.US)).//
				build());
	}

	/**
	 * Creates a AssertJ {@link Condition} that checks for the presence of a {@link Link} with the given
	 * {@link LinkRelation}.
	 *
	 * @param rel must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	protected Condition<MvcTestResult> linkWithRel(LinkRelation rel) {

		Assert.notNull(rel, "LinkRelation must not be null!");

		return new Condition<>(it -> hasLink(it.getMvcResult(), rel), "Expected to find link with relation %s!", rel);
	}

	@SuppressWarnings("null")
	protected LinkDiscoverer getDiscovererFor(MockHttpServletResponse response) {
		return links.getRequiredLinkDiscovererFor(response.getContentType());
	}

	@SneakyThrows
	@SuppressWarnings("null")
	private boolean hasLink(MvcResult result, LinkRelation rel) {

		var response = result.getResponse();
		var content = response.getContentAsString();
		var discoverer = links.getRequiredLinkDiscovererFor(response.getContentType());

		return discoverer.findLinkWithRel(rel, content).isPresent();
	}
}
