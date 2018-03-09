/*
 * Copyright 2018 the original author or authors.
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
package org.springsource.restbucks.statemachine;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.core.ResultPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springsource.restbucks.order.Order.OrderCreated;
import org.springsource.restbucks.statemachine.StateMachineConfiguration.AggregateStateMachineConfiguration;
import org.springsource.restbucks.statemachine.StateMachineConfiguration.AggregateStateMachineConfiguration.GuardedTransition;

/**
 * @author Oliver Gierke
 */
@RequiredArgsConstructor
public class StateMachineResultPostProcessor implements ResultPostProcessor.ForAggregate {

	private final AggregateStateMachine stateMachine;
	private final StateMachineConfiguration configuration;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.ResultPostProcessor.ByType#postProcess(java.lang.Object)
	 */
	@Override
	public Object postProcess(Object source) {

		if (source == null) {
			return source;
		}

		Class<? extends Object> aggregateType = source.getClass();

		if (!configuration.hasConfigurationFor(aggregateType)) {
			return source;
		}

		AggregateStateMachineConfiguration<?> metadata = configuration.getStateMachineFor(aggregateType);

		ProxyFactory factory = new ProxyFactory(source);
		factory.setProxyTargetClass(true);
		factory.addAdvice(new StateTransitioningMethodInterceptor(stateMachine, metadata));

		return factory.getProxy();
	}

	@EventListener
	void on(OrderCreated event) {
		stateMachine.startStateMachine(event.getOrder());
	}

	@RequiredArgsConstructor
	private static class StateTransitioningMethodInterceptor implements MethodInterceptor {

		private final AggregateStateMachine stateMachine;
		private final AggregateStateMachineConfiguration<?> metadata;

		/* 
		 * (non-Javadoc)
		 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {

			Object target = invocation.getThis();
			Method method = invocation.getMethod();

			if (!ReflectionUtils.isObjectMethod(method)) {

				GuardedTransition<Object> transition = (GuardedTransition<Object>) metadata.getOptionalTransition(method);
				transition.ifPreconditionMet(target, name -> stateMachine.executeStateTransition(target, name));
			}

			return invocation.proceed();
		}
	}
}
