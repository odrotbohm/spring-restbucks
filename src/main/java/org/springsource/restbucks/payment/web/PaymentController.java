/*
 * Copyright 2012-2013 the original author or authors.
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
@AllArgsConstructor(onConstructor = @_(@Autowired))
public class PaymentController {

	private final @NonNull PaymentService paymentService;
	private final @NonNull EntityLinks entityLinks;

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

		return new ResponseEntity<PaymentResource>(resource, HttpStatus.CREATED);
	}

	/**
	 * Shows the {@link Receipt} for the given order.
	 * 
	 * @param order
	 * @return
	 */
	@RequestMapping(value = PaymentLinks.RECEIPT, method = RequestMethod.GET)
	ResponseEntity<Resource<Receipt>> showReceipt(@PathVariable("id") Order order) {

		if (order == null || !order.isPaid() || order.isTaken()) {
			return new ResponseEntity<Resource<Receipt>>(HttpStatus.NOT_FOUND);
		}

		Payment payment = paymentService.getPaymentFor(order);

		if (payment == null) {
			return new ResponseEntity<Resource<Receipt>>(HttpStatus.NOT_FOUND);
		}

		return createReceiptResponse(payment.getReceipt());
	}

	/**
	 * Takes the {@link Receipt} for the given {@link Order} and thus completes the process.
	 * 
	 * @param order
	 * @return
	 */
	@RequestMapping(value = PaymentLinks.RECEIPT, method = RequestMethod.DELETE)
	ResponseEntity<Resource<Receipt>> takeReceipt(@PathVariable("id") Order order) {

		if (order == null || !order.isPaid()) {
			return new ResponseEntity<Resource<Receipt>>(HttpStatus.NOT_FOUND);
		}

		return createReceiptResponse(paymentService.takeReceiptFor(order));
	}

	/**
	 * Renders the given {@link Receipt} including links to the associated {@link Order} as well as a self link in case
	 * the {@link Receipt} is still available.
	 * 
	 * @param receipt
	 * @return
	 */
	private ResponseEntity<Resource<Receipt>> createReceiptResponse(Receipt receipt) {

		Order order = receipt.getOrder();

		Resource<Receipt> resource = new Resource<Receipt>(receipt);
		resource.add(entityLinks.linkToSingleResource(order));

		if (!order.isTaken()) {
			resource.add(entityLinks.linkForSingleResource(order).slash("receipt").withSelfRel());
		}

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
