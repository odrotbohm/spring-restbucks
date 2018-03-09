/*
 * Copyright 2013-2016 the original author or authors.
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

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.statemachine.CamundaStateMachine;
import org.springsource.restbucks.statemachine.StateMachineConfiguration;
import org.springsource.restbucks.statemachine.StateMachineConfiguration.AggregateStateMachineConfiguration;
import org.springsource.restbucks.statemachine.StateMachineConfiguration.AggregateStateMachineConfiguration.Result;
import org.springsource.restbucks.statemachine.StateMachineResultPostProcessor;

/**
 * Central application class containing both general application and web configuration as well as a main-method to
 * bootstrap the application using Spring Boot.
 *
 * @see #main(String[])
 * @see SpringApplication
 * @author Oliver Gierke
 */
@SpringBootApplication
@EnableAsync
public class Restbucks {

	public static String CURIE_NAMESPACE = "restbucks";

	public @Bean CurieProvider curieProvider() {
		return new DefaultCurieProvider(CURIE_NAMESPACE, new UriTemplate("/docs/{rel}.html"));
	}

	public @Bean StateMachineResultPostProcessor processor(PersistentEntities entities, ProcessEngine engine) {

		StateMachineConfiguration configuration = new StateMachineConfiguration();

		AggregateStateMachineConfiguration<Order> orderConfig = configuration.forAggregate(Order.class);

		orderConfig.withMethod("markPaid") //
				.boundTo("Message_PAYMENT_pay") //
				.withPrecondition(order -> Result.assertTrue(!order.isPaid(), "Already paid!"));

		orderConfig.withMethod("markInPreparation").boundTo("Message_ORDER_START_PREPARATION");
		orderConfig.withMethod("markPrepared").boundTo("Message_ORDER_PREPARED");
		orderConfig.withMethod("markTaken").boundTo("Message_ORDER_TAKEN");

		return new StateMachineResultPostProcessor(new CamundaStateMachine(engine, entities, configuration), configuration);
	}

	/**
	 * Bootstraps the application in standalone mode (i.e. java -jar).
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Restbucks.class, args);
	}

	@Aspect
	@Component
	@RequiredArgsConstructor
	static class ProxyUnwrapper {

		private final PersistentEntities entities;

		@Around("execution(public * org.springframework.data.repository.Repository+.*(..))")
		public Object unwrapProxies(ProceedingJoinPoint joinPoint) throws Throwable {

			if (joinPoint.getArgs().length == 0) {
				return joinPoint.proceed();
			}

			Object[] unwrappedArgs = Arrays.stream(joinPoint.getArgs()) //
					.map(it -> potentiallyUnwrap(it)) //
					.map(this::replaceProxies) //
					.collect(Collectors.toList()) //
					.toArray();

			return joinPoint.proceed(unwrappedArgs);
		}

		private static Object potentiallyUnwrap(Object source) {

			Object target = AopProxyUtils.getSingletonTarget(source);

			return target == null ? source : target;
		}

		private Object replaceProxies(Object source) {

			entities.getPersistentEntity(source.getClass()).ifPresent(it -> {

				PersistentPropertyAccessor propertyAccessor = it.getPropertyAccessor(source);

				UnwrappingPropertyHandler handler = new UnwrappingPropertyHandler(propertyAccessor);

				it.doWithProperties(handler);
				it.doWithAssociations(
						(SimpleAssociationHandler) association -> handler.doWithPersistentProperty(association.getInverse()));
			});

			return source;
		}

		@RequiredArgsConstructor
		private class UnwrappingPropertyHandler implements SimplePropertyHandler {

			private final PersistentPropertyAccessor accessor;

			/* 
			 * (non-Javadoc)
			 * @see org.springframework.data.mapping.SimplePropertyHandler#doWithPersistentProperty(org.springframework.data.mapping.PersistentProperty)
			 */
			@Override
			public void doWithPersistentProperty(PersistentProperty<?> property) {

				if (!property.isEntity() || !entities.getPersistentEntity(property.getType()).isPresent()) {
					return;
				}

				Object propertyValue = accessor.getProperty(property);
				Object unwrapped = potentiallyUnwrap(propertyValue);

				accessor.setProperty(property, replaceProxies(unwrapped));
			}
		}
	}
}
