/*
 * Copyright 2013-2019 the original author or authors.
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registers the OpenTelemetry Logback appender with the auto-configured OpenTelemetry
 * instance so that log events are exported via OTLP when the observability profile is active.
 *
 * @see <a href="https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot">OpenTelemetry with Spring Boot</a>
 */
@Configuration(proxyBeanMethods = false)
@Profile("observability")
@ConditionalOnClass(name = "io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender")
class OpenTelemetryLoggingConfiguration {

	@Bean
	OpenTelemetryAppenderInstaller openTelemetryAppenderInstaller(ApplicationContext applicationContext) {
		return new OpenTelemetryAppenderInstaller(applicationContext);
	}

	static class OpenTelemetryAppenderInstaller implements org.springframework.beans.factory.InitializingBean {

		private final ApplicationContext applicationContext;

		OpenTelemetryAppenderInstaller(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			Class<?> openTelemetryClass = Class.forName("io.opentelemetry.api.OpenTelemetry");
			Object openTelemetry = applicationContext.getBean(openTelemetryClass);
			Class<?> appenderClass = Class.forName("io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender");
			appenderClass.getMethod("install", openTelemetryClass).invoke(null, openTelemetry);
		}
	}
}
