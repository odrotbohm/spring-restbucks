package org.springsource.restbucks.order.web;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springsource.restbucks.drinks.Drink;
import org.springsource.restbucks.order.Location;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MeterRegistry.class)
class OrderObservabilityConfiguration {

	@Configuration(proxyBeanMethods = false)
	@Profile("!load-gen")
	static class DefaultConfig {

		@Bean
		AroundOrderControllerAspect aroundOrderControllerAspect(MeterRegistry meterRegistry) {
			return new AroundOrderControllerAspect(meterRegistry);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@Profile("load-gen")
	static class LoadTestConfig {

		@Bean
		ExceptionThrowingAroundOrderControllerAspect exceptionThrowingAroundOrderControllerAspect(
				MeterRegistry meterRegistry) {
			return new ExceptionThrowingAroundOrderControllerAspect(meterRegistry);
		}
	}

	@Aspect
	@AllArgsConstructor
	static class AroundOrderControllerAspect {

		private final MeterRegistry meterRegistry;

		@Around("execution(* org.springsource.restbucks.order.web.OrderController.placeOrder(..))")
		public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
			Object[] args = joinPoint.getArgs();
			LocationAndDrinks payload = (LocationAndDrinks) args[0];
			Location location = payload.getLocation();
			List<Drink> drinks = payload.getDrinks();
			drinks.forEach(
					drink -> Counter.builder("order.make") //
							.tags("name", drink.getName()) //
							.tags("location", location.name()) //
							.register(meterRegistry).increment()); //
			doAdditionalThings();
			return joinPoint.proceed();
		}

		protected void doAdditionalThings() {

		}

	}

	@Aspect
	static class ExceptionThrowingAroundOrderControllerAspect extends AroundOrderControllerAspect {

		private final AtomicLong counter = new AtomicLong();

		public ExceptionThrowingAroundOrderControllerAspect(MeterRegistry meterRegistry) {
			super(meterRegistry);
		}

		@Override
		protected void doAdditionalThings() {
			// Every now and then throw an exception
			long count = counter.incrementAndGet();
			if (count % ThreadLocalRandom.current().nextInt(7, 11) == 0) {
				throw new IllegalStateException("Boom!");
			}
		}
	}
}
