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
package org.springsource.restbucks.drinks;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.lang.Nullable;

/**
 * Additional configuration needed to produce Graal metadata to let some application properly work on it.
 *
 * @author Oliver Drotbohm
 */
@Configuration
@ImportRuntimeHints(DrinksNativeConfiguration.class)
class DrinksNativeConfiguration implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {

		System.out.println("Generating proxy for Drink");

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetClass(Drink.class);
		proxyFactory.setProxyTargetClass(true);

		var proxy = proxyFactory.getProxyClass(classLoader);

		hints.reflection().registerType(proxy, MemberCategory.INVOKE_DECLARED_METHODS);
	}
}
