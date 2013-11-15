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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.LiveBeansViewServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springsource.restbucks.Restbucks.WebConfiguration;

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
		return new Class<?>[0];
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer#getServletConfigClasses()
	 */
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { WebConfiguration.class };
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

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer#onStartup(javax.servlet.ServletContext)
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);

		ServletRegistration servlet = servletContext.addServlet("liveBeansView", LiveBeansViewServlet.class);
		servlet.addMapping("/live");
	}
}
