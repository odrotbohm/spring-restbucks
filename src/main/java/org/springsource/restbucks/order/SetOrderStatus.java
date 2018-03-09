package org.springsource.restbucks.order;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

//@Component
public class SetOrderStatus implements ExecutionListener {

	@Autowired private OrderRepository repository;

	@Override
	public void notify(DelegateExecution context) throws Exception {
		long orderId = Long.valueOf(context.getProcessBusinessKey());
		Order order = repository.findById(orderId).orElseThrow(() -> new IllegalStateException("No order found!"));
		// order.setStatus(context.getCurrentActivityName());
		repository.save(order);
	}
}
