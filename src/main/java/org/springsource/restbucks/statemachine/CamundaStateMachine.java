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
import java.util.function.Function;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.data.mapping.IdentifierAccessor;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springsource.restbucks.statemachine.StateMachineConfiguration.AggregateStateMachineConfiguration;
import org.springsource.restbucks.statemachine.StateMachineConfiguration.AggregateStateMachineConfiguration.GuardedTransition;

@RequiredArgsConstructor
public class CamundaStateMachine implements AggregateStateMachine {

	private final ProcessEngine engine;
	private final PersistentEntities entities;
	private final StateMachineConfiguration configuration;

	/*
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.statemachine.AggregateStateMachine#startStateMachine(java.lang.Object)
	 */
	@Override
	public void startStateMachine(Object aggregate) {

		String processId = aggregate.getClass().getSimpleName().toLowerCase();

		engine.getRuntimeService().startProcessInstanceByKey(processId, getIdentifier(aggregate));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.statemachine.AggregateStateMachine#executeStateTransition(java.lang.Object, java.util.function.Function)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T, S> S executeStateTransition(T aggregate, Function<T, S> function) {

		Class<? extends T> type = (Class<? extends T>) aggregate.getClass();
		AggregateStateMachineConfiguration<T> aggregateConfig = configuration.getStateMachineFor(type);

		// Function -> Method
		Method method = null;

		GuardedTransition<T> transition = aggregateConfig.getRequiredTransition(method);

		transition.ifPreconditionMet(aggregate, name -> executeStateTransition(aggregate, name));

		return function.apply(aggregate);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springsource.restbucks.statemachine.AggregateStateMachine#executeStateTransition(java.lang.Object, java.lang.String)
	 */
	@Override
	public void executeStateTransition(Object aggregate, String transition) {

		String identifier = getIdentifier(aggregate);

		engine.getRuntimeService() //
				.createMessageCorrelation(transition) //
				.processInstanceBusinessKey(identifier) //
				.correlate();
	}

	private String getIdentifier(Object aggregate) {

		PersistentEntity<?, ? extends PersistentProperty<?>> entity = entities
				.getRequiredPersistentEntity(aggregate.getClass());
		IdentifierAccessor accessor = entity.getIdentifierAccessor(aggregate);

		return String.valueOf(accessor.getRequiredIdentifier());
	}
}
