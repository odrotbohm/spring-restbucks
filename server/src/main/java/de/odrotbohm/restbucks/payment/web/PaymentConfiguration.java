/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odrotbohm.restbucks.payment.web;

import de.odrotbohm.restbucks.payment.CreditCard;
import de.odrotbohm.restbucks.payment.CreditCardNumber;
import de.odrotbohm.restbucks.payment.Payment.Receipt;

import org.springframework.boot.jackson.JacksonMixin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.MediaTypeConfigurationCustomizer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
class PaymentConfiguration {

	@Bean
	MediaTypeConfigurationCustomizer<HalFormsConfiguration> paymentHalFormsCustomization() {
		return config -> config.withPattern(CreditCardNumber.class, CreditCardNumber.REGEX);
	}

	@JacksonMixin(Receipt.class)
	static abstract class ReceiptMixin {

		@JsonIgnore
		abstract Object getOrder();
	}

	@JacksonMixin(CreditCard.class)
	@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
	static abstract class CreditCardMixin {}
}
