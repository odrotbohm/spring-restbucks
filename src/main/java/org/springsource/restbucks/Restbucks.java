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
package org.springsource.restbucks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 * Central application class containing both general application and web configuration as well as a main-method to
 * bootstrap the app using Spring Boot.
 * 
 * @see #main(String[])
 * @see SpringApplication
 * @author Oliver Gierke
 */
public class Restbucks extends SpringBootServletInitializer {

	public static String CURIE_NAMESPACE = "restbucks";

	/**
	 * Bootstraps the application in standalone mode (i.e. java -jar).
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(WebConfiguration.class, args);
	}

	/**
	 * Allows the application to be started when being deployed into a Servlet 3 container.
	 * 
	 * @see org.springframework.boot.web.SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebConfiguration.class);
	}

	@Configuration
	@EnableAsync
	@EnableAutoConfiguration
	// TODO: Remove once Boot's #236 is merged
	@EnableJpaRepositories
	@ComponentScan(includeFilters = @Filter(Service.class), useDefaultFilters = false)
	static class ApplicationConfiguration {

	}

	/**
	 * Web specific configuration.
	 * 
	 * @author Oliver Gierke
	 */
	@Configuration
	@Import({ ApplicationConfiguration.class, RepositoryRestMvcConfiguration.class })
	@ComponentScan(excludeFilters = @Filter({ Service.class, Configuration.class }))
	static class WebConfiguration {

		@Bean
		public CurieProvider curieProvider() {
			return new DefaultCurieProvider(CURIE_NAMESPACE, new UriTemplate("http://localhost:8080/rels/{rel}"));
		}
	}
}
