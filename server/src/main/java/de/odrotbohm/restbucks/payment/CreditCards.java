/*
 * Copyright 2012-2019 the original author or authors.
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
package de.odrotbohm.restbucks.payment;

import java.util.Optional;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository to access {@link CreditCard} instances.
 *
 * @author Oliver Gierke
 */
@SecondaryPort
interface CreditCards extends CrudRepository<CreditCard, CreditCardNumber> {

	/**
	 * Returns the {@link CreditCard} associated with the given {@link CreditCardNumber}.
	 *
	 * @param number must not be {@literal null}.
	 * @return
	 */
	Optional<CreditCard> findByNumber(CreditCardNumber number);
}
