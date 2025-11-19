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

import lombok.RequiredArgsConstructor;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.ConverterBuilder;
import org.springframework.data.convert.ConverterBuilder.ConverterAware;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Central application class containing both general application and web configuration as well as a main-method to
 * bootstrap the application using Spring Boot.
 *
 * @see #main(String[])
 * @see SpringApplication
 * @author Oliver Drotbohm
 */
@EnableAsync
@ConfigurationPropertiesScan
@Modulithic(sharedModules = "core")
@SpringBootApplication
public class Restbucks {

	public static String CURIE_NAMESPACE = "restbucks";

	@Bean
	CurieProvider curieProvider() {
		return new DefaultCurieProvider(CURIE_NAMESPACE, UriTemplate.of("/docs/{rel}.html"));
	}

	/**
	 * Bootstraps the application in standalone mode (i.e. java -jar).
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Restbucks.class, args);
	}

	@Configuration(proxyBeanMethods = false)
	@RequiredArgsConstructor
	static class PersistenceCustomizations extends AbstractJdbcConfiguration {

		private static final MonetaryAmountFormat FORMAT = MonetaryFormats.getAmountFormat(Locale.ROOT);

		private final ApplicationContext context;

		@Bean
		ConverterAware monetaryAmountConverter() {

			return ConverterBuilder.writing(MonetaryAmount.class, String.class, this::convert)
					.andReading(this::convertToEntityAttribute);
		}

		@Bean
		ConverterAware yearAttributeConverter() {
			return ConverterBuilder.writing(Year.class, Integer.class, Year::getValue).andReading(Year::of);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration#userConverters()
		 */
		@Override
		protected List<?> userConverters() {

			context.getBeansOfType(ConverterAware.class).values();

			return new ArrayList<>(context.getBeansOfType(ConverterAware.class).values());
		}

		@Nullable
		String convert(MonetaryAmount amount) {

			return amount == null ? null
					: String.format("%s %s", amount.getCurrency().toString(), amount.getNumber().toString());
		}

		Money convertToEntityAttribute(String source) {

			if (source == null) {
				return null;
			}

			try {
				return Money.parse(source);
			} catch (RuntimeException e) {

				try {
					return Money.parse(source, FORMAT);
				} catch (RuntimeException inner) {

					// Propagate the original exception in case the fallback fails
					throw e;
				}
			}
		}
	}
}
