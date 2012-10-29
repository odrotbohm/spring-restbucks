/*
 * Copyright 2012 the original author or authors.
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
package org.springsource.restbucks.payment.web;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springsource.restbucks.core.MonetaryAmount;
import org.springsource.restbucks.order.Order;
import org.springsource.restbucks.payment.CreditCard;
import org.springsource.restbucks.payment.CreditCardNumber;
import org.springsource.restbucks.payment.CreditCardPayment;
import org.springsource.restbucks.payment.Payment;
import org.springsource.restbucks.payment.Payment.Receipt;
import org.springsource.restbucks.payment.PaymentService;

/**
 * Spring MVC controller to handle payments for an {@link Order}.
 * 
 * @author Oliver Gierke
 */
@Controller
@RequestMapping("/orders/{id}")
@ExposesResourceFor(Payment.class)
public class PaymentController {

	private final PaymentService paymentService;
	private final EntityLinks entityLinks;

	/**
	 * Creates a new {@link PaymentController} using the given {@link PaymentService}.
	 * 
	 * @param paymentService must not be {@literal null}.
	 * @param entityLinks must not be {@literal null}.
	 */
	@Autowired
	public PaymentController(PaymentService paymentService, EntityLinks entityLinks) {

		Assert.notNull(paymentService, "PaymentService must not be null!");
		Assert.notNull(entityLinks, "EntityLinks must not be null!");

		this.paymentService = paymentService;
		this.entityLinks = entityLinks;
	}

	/**
	 * Accepts a payment for an {@link Order}
	 * 
	 * @param order the {@link Order} to process the payment for. Retrieved from the path variable and converted into an
	 *          {@link Order} instance by Spring Data's {@link DomainClassConverter}. Will be {@literal null} in case no
	 *          {@link Order} with the given id could be found.
	 * @param number the {@link CreditCardNumber} unmarshalled from the request payload.
	 * @return
	 */
	@RequestMapping(value = PaymentLinks.PAYMENT, method = RequestMethod.PUT)
	ResponseEntity<PaymentResource> submitPayment(@PathVariable("id") Order order, @RequestBody CreditCardNumber number) {

		if (order == null || order.isPaid()) {
			return new ResponseEntity<PaymentResource>(HttpStatus.NOT_FOUND);
		}

		CreditCardPayment payment = paymentService.pay(order, number);

		PaymentResource resource = new PaymentResource(order.getPrice(), payment.getCreditCard());
		resource.add(entityLinks.linkToSingleResource(order));

		return new ResponseEntity<PaymentResource>(resource, HttpStatus.OK);
	}

	/**
	 * Shows the {@link Receipt} for the given {@link Order}.
	 * 
	 * @param order
	 * @return
	 */
	@RequestMapping(value = PaymentLinks.RECEIPT, method = RequestMethod.DELETE)
	ResponseEntity<Resource<Receipt>> showReceipt(@PathVariable("id") Order order) {

		if (!order.isPaid()) {
			return new ResponseEntity<Resource<Receipt>>(HttpStatus.NOT_FOUND);
		}

		Payment payment = paymentService.getPaymentFor(order);

		Resource<Receipt> resource = new Resource<Receipt>(payment.getReceipt());
		resource.add(entityLinks.linkToSingleResource(order));
		resource.add(entityLinks.linkForSingleResource(order).slash("receipt").withSelfRel());

		return new ResponseEntity<Resource<Receipt>>(resource, HttpStatus.OK);
	}

	/**
	 * Resource implementation for payment results.
	 * 
	 * @author Oliver Gierke
	 */
	@Data
	@EqualsAndHashCode(callSuper = true)
	static class PaymentResource extends ResourceSupport {

		private final MonetaryAmount amount;
		private final CreditCard creditCard;
	}
}
