/*
 * Copyright 2013-2018 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.util.Locale;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class to derive concrete web test classes from.
 *
 * @author Oliver Gierke
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AbstractWebIntegrationTest {

	@Autowired WebApplicationContext context;
	@Autowired LinkDiscoverers links;

	protected MockMvc mvc;

	@Before
	public void setUp() {

		mvc = MockMvcBuilders.webAppContextSetup(context).//
				defaultRequest(MockMvcRequestBuilders.get("/").locale(Locale.US)).//
				build();
	}

	/**
	 * Creates a {@link ResultMatcher} that checks for the presence of a link with the given rel.
	 *
	 * @param rel
	 * @return
	 */
	protected ResultMatcher linkWithRelIsPresent(LinkRelation rel) {
		return new LinkWithRelMatcher(rel, true);
	}

	/**
	 * Creates a {@link ResultMatcher} that checks for the non-presence of a link with the given rel.
	 *
	 * @param rel
	 * @return
	 */
	protected ResultMatcher linkWithRelIsNotPresent(LinkRelation rel) {
		return new LinkWithRelMatcher(rel, false);
	}

	protected LinkDiscoverer getDiscovererFor(MockHttpServletResponse response) {
		return links.getRequiredLinkDiscovererFor(response.getContentType());
	}

	private class LinkWithRelMatcher implements ResultMatcher {

		private final LinkRelation rel;
		private final boolean present;

		public LinkWithRelMatcher(LinkRelation rel, boolean present) {

			this.rel = rel;
			this.present = present;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.test.web.servlet.ResultMatcher#match(org.springframework.test.web.servlet.MvcResult)
		 */
		@Override
		public void match(MvcResult result) throws Exception {

			MockHttpServletResponse response = result.getResponse();
			String content = response.getContentAsString();
			LinkDiscoverer discoverer = links.getRequiredLinkDiscovererFor(response.getContentType());

			assertThat(discoverer.findLinkWithRel(rel, content)).matches(it -> it.isPresent() == present);
		}
	}
}
