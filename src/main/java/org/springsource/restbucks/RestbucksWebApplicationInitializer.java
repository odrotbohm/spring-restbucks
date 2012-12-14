/*
 * Copyright 2012 the original author or authors.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.webmvc.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.ResourceProcessorInvokingHandlerAdapter;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springsource.restbucks.payment.web.PaymentController;
import org.springsource.restbucks.support.RepositoryLinkMetadataFactory;
import org.springsource.restbucks.support.RestResourceEntityLinks;

/**
 * Servlet 3.0 {@link WebApplicationInitializer} using Spring 3.2 convenient base class
 * {@link AbstractAnnotationConfigDispatcherServletInitializer}. It essentially sets up a root application context from
 * {@link ApplicationConfig}, and a dispatcher servlet application context from {@link RepositoryRestMvcConfiguration}
 * (enabling Spring Data REST) and {@link WebConfiguration} for general Spring MVC customizations.
 * 
 * @author Oliver Gierke
 */
public class RestbucksWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer#getRootConfigClasses()
	 */
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { ApplicationConfig.class };
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer#getServletConfigClasses()
	 */
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { RepositoryRestMvcConfiguration.class, WebConfiguration.class };
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer#getServletMappings()
	 */
	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer#getServletFilters()
	 */
	@Override
	protected javax.servlet.Filter[] getServletFilters() {
		return new javax.servlet.Filter[] { new OpenEntityManagerInViewFilter() };
	}

	/**
	 * Web layer configuration enabling Spring MVC, Spring Hateoas {@link EntityLinks}.
	 * 
	 * @author Oliver Gierke
	 */
	@Configuration
	@EnableWebMvc
	@EnableEntityLinks
	@ComponentScan(excludeFilters = @Filter({ Service.class, Configuration.class }))
	public static class WebConfiguration extends WebMvcConfigurationSupport {

		@Autowired
		ApplicationContext context;

		/**
		 * Overrride {@link RequestMappingHandlerAdapter} by a custom one provided by Spring Data REST which will invoke
		 * {@link ResourceProcessor} instances registered in the {@link ApplicationContext}. See
		 * {@link PaymentOrderResourceProcessor} for example.
		 */
		@Bean
		@Override
		public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
			RequestMappingHandlerAdapter original = super.requestMappingHandlerAdapter();
			return new ResourceProcessorInvokingHandlerAdapter(original);
		}

		/**
		 * Registers a {@link DomainClassConverter} to allow usage of domain types as Spring MVC controller method
		 * arguments. As an example see {@link PaymentController} methods using {@link Order} in the method signatures
		 * directly instead of a raw {@link Long} or {@link String} and manually resolving the {@link Order} instance.
		 * 
		 * @return
		 */
		@Bean
		public DomainClassConverter<?> domainClassConverter() {
			return new DomainClassConverter<FormattingConversionService>(mvcConversionService());
		}

		/**
		 * Registers a custom {@link EntityLinks} instance for all the types managed by Spring Data REST.
		 * 
		 * @return
		 */
		@Bean
		public RestResourceEntityLinks restResourceEntityLinks() {

			Repositories repositories = new Repositories(context);
			RepositoryLinkMetadataFactory factory = new RepositoryLinkMetadataFactory(repositories);
			return new RestResourceEntityLinks(factory, "");
		}
	}
}
