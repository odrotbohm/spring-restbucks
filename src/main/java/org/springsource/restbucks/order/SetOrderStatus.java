package org.springsource.restbucks.order;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetOrderStatus implements ExecutionListener {
  
  @Autowired
  private OrderRepository repository;

  @Override
  public void notify(DelegateExecution context) throws Exception {    
    long orderId = Long.valueOf( context.getProcessBusinessKey() );    
    Order order = repository.findOne(orderId);
    order.setStatus(context.getCurrentActivityName());
    repository.save(order);    
  }

}
