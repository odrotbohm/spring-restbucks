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

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.repository.support.Repositories;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Integration test to bootstrap the root {@link ApplicationContext}.
 * 
 * @author Oliver Gierke
 */
public class ApplicationIntegrationTest {

	@Test
	public void initializesRootApplicationContext() {

		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {

			context.register(Restbucks.class);
			context.refresh();

			new Repositories(context);
		}
	}

	@Test
	public void initializesWebApplicationContext() {

		try (AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext()) {

			applicationContext.register(Restbucks.class);
			applicationContext.setServletContext(new MockServletContext());
			applicationContext.refresh();
		}
	}
}
