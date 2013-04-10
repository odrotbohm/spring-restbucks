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

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springsource.restbucks.RestbucksWebApplicationInitializer.WebConfiguration;

/**
 * Integration test to bootstrap the root {@link ApplicationContext}.
 * 
 * @author Oliver Gierke
 */
public class ApplicationIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	ApplicationContext context;

	@Test
	public void initializesRootApplicationContext() {
		new Repositories(context);
	}

	@Test
	@Ignore
	public void initializesWebApplicationContext() {

		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(RepositoryRestMvcConfiguration.class);
		applicationContext.register(WebConfiguration.class);
		applicationContext.setParent(context);
		applicationContext.refresh();
	}
}
