/*
 * Copyright 2012-2021 the original author or authors.
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
package org.springsource.restbucks.payment;

import lombok.extern.slf4j.Slf4j;

import java.time.Month;
import java.time.Year;

import org.springframework.stereotype.Service;

/**
 * Initializing component to create a default {@link CreditCard} in the system.
 *
 * @author Oliver Gierke
 */
@Service
@Slf4j
class PaymentInitializer {

	public PaymentInitializer(CreditCards creditCards) {

		if (creditCards.count() > 0) {
			return;
		}

		CreditCardNumber number = CreditCardNumber.of("1234123412341234");
		CreditCard creditCard = new CreditCard(number, "Oliver Gierke", Month.DECEMBER, Year.of(2099));

		creditCard = creditCards.save(creditCard);

		LOG.info("Credit card {} created!", creditCard);
	}
}
