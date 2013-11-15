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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

/**
 * Central application class containing both general application and web configuration as well as a main-method to
 * bootstrap the app using Spring Boot.
 * 
 * @see #main(String[])
 * @see SpringApplication
 * @author Oliver Gierke
 */
public class Restbucks {

	/**
	 * Bootstraps the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(WebConfiguration.class, args);
	}

	@Configuration
	@EnableAsync
	@EnableAutoConfiguration
	@ComponentScan(includeFilters = @Filter(Service.class), useDefaultFilters = false)
	static class ApplicationConfig {

	}

	@Configuration
	@EnableHypermediaSupport
	@Import({ ApplicationConfig.class, RepositoryRestMvcConfiguration.class })
	@ComponentScan(excludeFilters = @Filter({ Service.class, Configuration.class }))
	static class WebConfiguration extends DelegatingWebMvcConfiguration {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#configureContentNegotiation(org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer)
		 */
		@Override
		public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			configurer.defaultContentType(MediaType.APPLICATION_JSON);
		}
	}
}
