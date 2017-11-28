package org.springsource.restbucks.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springsource.restbucks.order.web.MethodNotAllowedBecauseOfCurrentStateException;

@Service
@Transactional
public class OrderService {
  
  @Autowired
  private OrderRepository repository;

  @Autowired
  private ProcessEngine engine;
  
  public void createNewOrder(Order o) {
    o = repository.save(o);
    engine.getRuntimeService().startProcessInstanceByKey(
        "order", 
        String.valueOf(o.getId()));
  }
  
  private void correlateToStateMachine(Order order, String messageName) {
    try {
    // throws an exception if not waiting for this message
    engine.getRuntimeService()
      .createMessageCorrelation(messageName) //
      .processInstanceBusinessKey(String.valueOf(order.getId())) //
      .correlate();
    } catch (MismatchingMessageCorrelationException ex) {
      throw new MethodNotAllowedBecauseOfCurrentStateException(ex);
    }
  }

  /**
   * Marks the {@link Order} as payed.
   */
  public void markPaid(Order order) {
    correlateToStateMachine(order ,"Message_PAYMENT_pay");

    // Should be probably better moved to the flow definition to make sure it
    // is always raised when this state transition is done
    order.raiseOrderPaid();
  }

  /**
   * Marks the {@link Order} as in preparation.
   */
  public void markInPreparation(Order order) {
    correlateToStateMachine(order, "Message_ORDER_START_PREPARATION");
  }

  /**
   * Marks the {@link Order} as prepared.
   */
  public void markPrepared(Order order) {
    correlateToStateMachine(order, "Message_ORDER_PREPARED");
    // Hmm - have to change entity in order to avoid 304.    
  }

  public void markTaken(Order order) {
    correlateToStateMachine(order, "Message_ORDER_TAKEN");
  }
  
  public void markReceiptTaken(Order order) {
    correlateToStateMachine(order, "Message_PAYMENT_receipt");    
  }
  public void markDeleted(Order order) {
    correlateToStateMachine(order, "Message_ORDER_delete");
  }

  
  public Collection<String> getPossibleLinks(Order order, String resourceType) {    
    String processInstanceId = getAssociatedProcessInstanceId(order);
    if (processInstanceId==null) { // could be null as soon as workflow instance is ended
      return new ArrayList<String>();
    }
    
    List<EventSubscription> eventSubscriptions = engine.getRuntimeService().createEventSubscriptionQuery()
      .processInstanceId(processInstanceId)
      .eventType("message")
      .list();
    
    return eventSubscriptions.stream()
        // we assume a message name of "Message_[ResourceType]_[LinkName]"
        .filter(eventSub -> eventSub.getEventName().startsWith("Message_" + resourceType)) 
        .map(eventSub -> eventSub.getEventName().substring(eventSub.getEventName().lastIndexOf("_")+1))
        .collect(Collectors.toList());
  }

  public String getAssociatedProcessInstanceId(Order order) {
    ProcessInstance processInstance = engine.getRuntimeService().createProcessInstanceQuery() //
        .processInstanceBusinessKey(String.valueOf(order.getId())) //    
        .singleResult();
    if (processInstance==null) {
      return null;
    }
    return processInstance.getId();
  }
  
  public String getStatus(Order order) {
    // This does not make total sense generically seen - as the order could be in multiple
    // status at the same time. We could probably set an additional info status
    // from the workflow when certain milestones are reached.
    String processInstanceId = getAssociatedProcessInstanceId(order);

    // Process Instance ActivityInstance level
    ActivityInstance instance = engine.getRuntimeService().getActivityInstance(processInstanceId);
    // we have no parallelism - so exactly one child:
    ActivityInstance[] childActivityInstances = instance.getChildActivityInstances();
    if (childActivityInstances.length!=1) {
      throw new IllegalStateException("Please adjust status information method when updating the BPMN model.");
    }
    return childActivityInstances[0].getActivityName();
  }



}
