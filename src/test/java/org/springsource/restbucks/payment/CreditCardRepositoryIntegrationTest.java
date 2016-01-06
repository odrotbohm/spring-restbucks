/*
 * Copyright 2012-2015 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Month;
import java.time.Year;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springsource.restbucks.AbstractIntegrationTest;

/**
 * Integration tests for {@link CreditCardRepository}.
 * 
 * @author Oliver Gierke
 */
@DirtiesContext
public class CreditCardRepositoryIntegrationTest extends AbstractIntegrationTest {

	@Autowired CreditCardRepository repository;

	@Test
	public void createsCreditCard() {

		CreditCard creditCard = repository.save(createCreditCard());

		Optional<CreditCard> result = repository.findByNumber(creditCard.getNumber());

		assertThat(result.isPresent(), is(true));
		assertThat(result.get(), is(creditCard));
	}

	public static CreditCard createCreditCard() {

		CreditCardNumber number = new CreditCardNumber("4321432143214321");
		return new CreditCard(number, "Oliver Gierke", Month.DECEMBER, Year.of(2020));
	}
}
