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
package org.springsource.restbucks;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springsource.restbucks.drinks.Drink;

/**
 * Additional configuration needed to produce Graal metadata to let some application properly work on it.
 *
 * @author Oliver Drotbohm
 */
@Configuration

// Due to DrinksOptions.BY_NAME (i.e. the usage of a domain type with Spring Data's TypedSort)
// @AotProxyHint(targetClass = Drink.class, proxyFeatures = ProxyBits.IS_STATIC)
class NativeConfiguration implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		Sort.sort(Drink.class).by(Drink::getName).ascending();
	}
}
