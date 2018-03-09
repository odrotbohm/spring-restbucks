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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.util.Assert;

/**
 * @author Oliver Gierke
 */
public class StateMachineConfiguration {

	private final Map<Class<?>, AggregateStateMachineConfiguration<?>> machines = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> AggregateStateMachineConfiguration<T> forAggregate(Class<T> aggregateType) {

		return (AggregateStateMachineConfiguration<T>) machines.computeIfAbsent(aggregateType, //
				it -> new AggregateStateMachineConfiguration<T>(aggregateType));
	}

	public boolean hasConfigurationFor(Class<?> aggregateType) {
		return machines.containsKey(aggregateType);
	}

	@SuppressWarnings("unchecked")
	public <T> AggregateStateMachineConfiguration<T> getStateMachineFor(Class<? extends T> aggregateType) {

		if (!hasConfigurationFor(aggregateType)) {
			throw new IllegalArgumentException("No state machine found for " + aggregateType.getName());
		}

		return (AggregateStateMachineConfiguration<T>) machines.get(aggregateType);
	}

	@RequiredArgsConstructor
	public static class AggregateStateMachineConfiguration<T> {

		private final @Getter Class<T> aggregateType;
		private final Map<String, String> methodsToStateTransition = new HashMap<>();
		private final Map<String, Function<T, Result>> preconditions = new HashMap<>();

		public boolean hasTransition(Method method) {
			return methodsToStateTransition.containsKey(method.getName());
		}

		public GuardedTransition<T> getOptionalTransition(Method method) {

			String transition = methodsToStateTransition.get(method.getName());

			return transition == null ? new NoTransition<T>() : createTransition(method, transition);
		}

		public GuardedTransition<T> getRequiredTransition(Method method) {

			String transition = methodsToStateTransition.get(method.getName());

			if (transition == null) {
				throw new IllegalArgumentException(String.format("No transition found for %s!", method));
			}

			return createTransition(method, transition);
		}

		private GuardedTransition<T> createTransition(Method method, String transition) {

			Function<T, Result> precondition = Optional.ofNullable(preconditions.get(method.getName()))
					.orElse(__ -> Result.success());

			return Transition.of(transition, precondition);
		}

		public TransitionBuilder withMethod(Method method) {
			return new TransitionBuilder(method.getName());
		}

		public TransitionBuilder withMethod(String methodName) {
			return new TransitionBuilder(methodName);
		}

		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		public class TransitionBuilder {

			private final String methodName;

			public TransitionBuilder boundTo(String transition) {

				AggregateStateMachineConfiguration<T> config = AggregateStateMachineConfiguration.this;

				config.methodsToStateTransition.put(methodName, transition);
				return this;
			}

			public TransitionBuilder withPrecondition(Function<T, Result> precondition) {

				AggregateStateMachineConfiguration<T> config = AggregateStateMachineConfiguration.this;

				config.preconditions.put(methodName, precondition);

				return this;
			}

			public TransitionBuilder andMethod(String name) {

				AggregateStateMachineConfiguration<T> config = AggregateStateMachineConfiguration.this;

				return config.withMethod(name);
			}
		}

		interface GuardedTransition<T> {
			default void ifPreconditionMet(T aggregate, Consumer<String> withName) {}
		}

		private static class NoTransition<T> implements GuardedTransition<T> {}

		/**
		 * A transition with a given name applying a precondition on an aggregate before actually performing the transition.
		 *
		 * @author Oliver Gierke
		 */
		@RequiredArgsConstructor(staticName = "of")
		private static class Transition<T> implements GuardedTransition<T> {

			private final String name;
			private final Function<T, Result> precondition;

			/*
			 * (non-Javadoc)
			 * @see org.springsource.restbucks.statemachine.StateMachineConfiguration.AggregateStateMachineConfiguration.GuardedTransition#ifPreconditionMet(java.lang.Object, java.util.function.Consumer)
			 */
			public void ifPreconditionMet(T aggregate, Consumer<String> withName) {

				precondition.apply(aggregate).rejectIfFailure();

				withName.accept(name);
			}
		}

		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		public static class Result {

			private static final Result SUCCESS = new Result(Optional.empty());

			private final Optional<Supplier<? extends RuntimeException>> exception;

			/**
			 * Returns a successful result which leads to the state transition to be performed.
			 * 
			 * @return
			 */
			public static Result success() {
				return SUCCESS;
			}

			/**
			 * Returns a failed {@link Result} to expose the {@link RuntimeException} provided by the given {@link Supplier}.
			 * 
			 * @param exception must not be {@literal null}.
			 * @return
			 */
			public static Result failed(Supplier<? extends RuntimeException> exception) {

				Assert.notNull(exception, "Exception supplier must not be null!");

				return new Result(Optional.of(exception));
			}

			/**
			 * Returns a failed Result to expose an {@link IllegalStateException} using the given message and arguments.
			 * 
			 * @param message must not be {@literal null} or empty.
			 * @param arguments must not be {@literal null}.
			 * @return
			 */
			public static Result failed(String message, Object... arguments) {

				Assert.hasText(message, "Message must not be null or empty!");
				Assert.notNull(arguments, "Arguments must not be null!");

				return new Result(Optional.of(() -> new IllegalStateException(String.format(message, arguments))));
			}

			/**
			 * Returns a {@link Result} based on whether the given condition evaluates to {@literal true} using the given
			 * message and arguments to create an {@link IllegalStateException} in case of the condition evaluating to
			 * {@literal false}.
			 * 
			 * @param condition the condition to evaluate.
			 * @param message
			 * @param arguments
			 * @return
			 */
			public static Result assertTrue(boolean condition, String message, Object... arguments) {
				return condition ? Result.success() : Result.failed(message, arguments);
			}

			public boolean succeed() {
				return !isFailure();
			}

			public boolean isFailure() {
				return exception.isPresent();
			}

			public void rejectIfFailure() {

				exception.ifPresent(it -> {
					throw it.get();
				});
			}
		}
	}
}
