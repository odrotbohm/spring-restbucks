/*
 * Copyright 2012-2016 the original author or authors.
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
package org.springsource.restbucks.order;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.money.MonetaryAmount;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springsource.restbucks.core.AbstractAggregateRoot;
import org.springsource.restbucks.payment.OrderPaid;

/**
 * An order.
 *
 * @author Oliver Gierke
 */
@Entity
@Getter
@ToString(exclude = "lineItems")
@Table(name = "RBOrder")
public class Order extends AbstractAggregateRoot {

	private final Location location;
	private final LocalDateTime orderedDate;

	/**
	 * read-only attribute to cary informal representation of current status
	 */
	private String status;

	@OrderColumn //
	@Column(unique = true) //
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) //
	private final List<LineItem> lineItems = new ArrayList<>();

	/**
	 * Creates a new {@link Order} for the given {@link LineItem}s and {@link Location}.
	 * 
	 * @param lineItems must not be {@literal null}.
	 * @param location
	 */
	public Order(Collection<LineItem> lineItems, Location location) {
		this.location = location == null ? Location.TAKE_AWAY : location;
		this.lineItems.addAll(lineItems);
		this.orderedDate = LocalDateTime.now();		
	}

	/**
	 * Creates a new {@link Order} containing the given {@link LineItem}s.
	 * 
	 * @param items must not be {@literal null}.
	 */
	public Order(LineItem... items) {
		this(Arrays.asList(items), null);
	}

	Order() {
		this(new LineItem[0]);
	}

	/**
	 * Returns the price of the {@link Order} calculated from the contained items.
	 * 
	 * @return will never be {@literal null}.
	 */
	public MonetaryAmount getPrice() {

		return lineItems.stream().//
				map(LineItem::getPrice).//
				reduce(MonetaryAmount::add).orElse(Money.of(0.0, "EUR"));
	}
	
	
	public String getStatus() {
	  // Todo: Maybe set a read-only status from the flow for simplicity of reading
	  // a senseful state probably being different from the exact workflow state
	  return status;
	}

  public void raiseOrderPaid() {
    // TODO: Implement differently
    registerEvent(new OrderPaid(getId()));
  }

  public void setStatus(String status) {
    this.status = status;
  }

 
	
//
//	/**
//	 * Returns whether the {@link Order} has been paid already.
//	 * 
//	 * @return
//	 */
//	public boolean isPaid() {
//		return !this.status.equals(Status.PAYMENT_EXPECTED);
//	}
//
//	/**
//	 * Returns if the {@link Order} is ready to be taken.
//	 * 
//	 * @return
//	 */
//	public boolean isReady() {
//		return this.status.equals(Status.READY);
//	}
//
//	public boolean isTaken() {
//		return this.status.equals(Status.TAKEN);
//	}

//	/**
//	 * Enumeration for all the statuses an {@link Order} can be in.
//	 * 
//	 * @author Oliver Gierke
//	 */
//	public static enum Status {
//
//		/**
//		 * Placed, but not payed yet. Still changeable.
//		 */
//		PAYMENT_EXPECTED,
//
//		/**
//		 * {@link Order} was payed. No changes allowed to it anymore.
//		 */
//		PAID,
//
//		/**
//		 * The {@link Order} is currently processed.
//		 */
//		PREPARING,
//
//		/**
//		 * The {@link Order} is ready to be picked up by the customer.
//		 */
//		READY,
//
//		/**
//		 * The {@link Order} was completed.
//		 */
//		TAKEN;
//	}
}
