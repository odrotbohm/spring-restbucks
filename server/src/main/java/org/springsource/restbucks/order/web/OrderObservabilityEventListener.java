package org.springsource.restbucks.order.web;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springsource.restbucks.order.Order;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Profile;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnClass(MeterRegistry.class)
@Profile("observability")
class OrderObservabilityEventListener {

	private final MeterRegistry registry;

	OrderObservabilityEventListener(MeterRegistry registry) {
		this.registry = registry;
	}

	@ApplicationModuleListener
	public void onOrderCreated(Order.OrderCreated event) {
		Counter.builder("order.make") //
				.tags("location", event.location() != null ? event.location().name() : "NONE") //
				.register(registry).increment();
	}

	@ApplicationModuleListener
	public void onOrderLineItemCreated(Order.OrderLineItemCreated event) {
		Counter.builder("order.line-item.added") //
				.tags("name", event.lineItem().getName()) //
				.tags("milk", event.lineItem().getMilk().name()) //
				.tags("size", event.lineItem().getSize().name()) //
				.register(registry).increment();
	}

	@ApplicationModuleListener
	public void onOrderInPreparation(Order.OrderInPreparation event) {
		Counter.builder("order.in-preparation") //
				.register(registry).increment();
	}

	@ApplicationModuleListener
	public void onOrderTaken(Order.OrderTaken event) {
		Counter.builder("order.taken") //
				.register(registry).increment();
	}

	@ApplicationModuleListener
	public void onOrderPrepared(Order.OrderPrepared event) {
		Counter.builder("order.prepared") //
				.register(registry).increment();
	}

}
