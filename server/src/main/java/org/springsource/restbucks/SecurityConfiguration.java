/*
 * Copyright 2015-2021 the original author or authors.
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;

/**
 * Web security configuration enabling:
 * <ol>
 * <li>HTTP Basic authentication with Spring Security</li>
 * <li>Configuring Spring Session to expose the session using an HTTP header.</li>
 * </ol>
 *
 * @author Oliver Drotbohm
 */
@Configuration
@ConditionalOnWebApplication
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	/**
	 * Configures a the HTTP session to be exposed via an HTTP header.
	 */
	@Bean
	public HeaderHttpSessionIdResolver sessionStrategy() {
		return HeaderHttpSessionIdResolver.xAuthToken();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests().anyRequest().authenticated().and().//
				httpBasic().realmName("Spring RESTBucks");
	}
}
