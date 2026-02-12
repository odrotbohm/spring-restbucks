/*
 * Copyright 2012-2019 the original author or authors.
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
package de.odrotbohm.restbucks.payment.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import de.odrotbohm.restbucks.DTO;
import de.odrotbohm.restbucks.order.Order;
import de.odrotbohm.restbucks.order.Orders;
import de.odrotbohm.restbucks.payment.CreditCardNumber;
import de.odrotbohm.restbucks.payment.Payment;
import de.odrotbohm.restbucks.payment.Payment.Receipt;
import de.odrotbohm.restbucks.payment.PaymentFailed;
import de.odrotbohm.restbucks.payment.PaymentService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.money.MonetaryAmount;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring MVC controller to handle payments for an {@link Order}.
 *
 * @author Oliver Drotbohm
 */
@Controller
@RequestMapping("/orders/{id}")
@ExposesResourceFor(Payment.class)
@RequiredArgsConstructor
class PaymentController {

	private final @NonNull PaymentService paymentService;
	private final @NonNull PaymentLinks paymentLinks;
	private final @NonNull Orders orders;

	/**
	 * Accepts a payment for an {@link Order}
	 *
	 * @param order the {@link Order} to process the payment for. Retrieved from the path variable and converted into an
	 *          {@link Order} instance by Spring Data's {@link DomainClassConverter}. Will be {@literal null} in case no
	 *          {@link Order} with the given id could be found.
	 * @param number the {@link CreditCardNumber} unmarshalled from the request payload.
	 * @return
	 */
	@PutMapping(path = PaymentLinks.PAYMENT)
	ResponseEntity<?> submitPayment(@PathVariable("id") Order order, @RequestBody PaymentForm form) {

		if (order == null || order.isPaid()) {
			return ResponseEntity.notFound().build();
		}

		var payment = paymentService.pay(order, form.number());
		var model = new PaymentModel(order.getPrice(), payment.getCreditCardNumber().getId()) //
				.add(paymentLinks.getOrderLinks().linkToItemResource(order));

		var paymentUri = paymentLinks.getPaymentLink(order).toUri();

		return ResponseEntity.created(paymentUri).body(model);
	}

	/**
	 * Shows the {@link Receipt} for the given order.
	 *
	 * @param order
	 * @return
	 */
	@GetMapping(path = PaymentLinks.RECEIPT)
	HttpEntity<?> showReceipt(@PathVariable("id") Order order) {

		if (order == null || !order.isPaid() || order.isTaken()) {
			return ResponseEntity.notFound().build();
		}

		return paymentService.getPaymentFor(order) //
				.map(Payment::getReceipt) //
				.map(this::createReceiptResponse) //
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	/**
	 * Takes the {@link Receipt} for the given {@link Order} and thus completes the process.
	 *
	 * @param order
	 * @return
	 */
	@DeleteMapping(path = PaymentLinks.RECEIPT)
	HttpEntity<?> takeReceipt(@PathVariable("id") Order order) {

		if (order == null || !order.isPaid()) {
			return ResponseEntity.notFound().build();
		}

		return paymentService.takeReceiptFor(order) //
				.map(this::createReceiptResponse) //
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED));
	}

	/**
	 * Make sure that we translate {@link PaymentFailed} into 400.
	 *
	 * @param exception will never be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	@ExceptionHandler
	ResponseEntity<String> handle(PaymentFailed exception) {
		return ResponseEntity.badRequest().body(exception.getMessage());
	}

	/**
	 * Renders the given {@link Receipt} including links to the associated {@link Order} as well as a self link in case
	 * the {@link Receipt} is still available.
	 *
	 * @param receipt
	 * @return
	 */
	private HttpEntity<EntityModel<Receipt>> createReceiptResponse(Receipt receipt) {

		var orderLinks = paymentLinks.getOrderLinks();
		var order = orders.resolveRequired(receipt.getOrder());

		return ResponseEntity.ok(EntityModel.of(receipt)
				.add(orderLinks.linkToItemResource(order))
				.addIf(!order.isTaken(), () -> linkTo(methodOn(PaymentController.class).showReceipt(order)).withSelfRel()));
	}

	/**
	 * EntityModel implementation for payment results.
	 *
	 * @author Oliver Drotbohm
	 */
	@Data
	@EqualsAndHashCode(callSuper = true)
	static class PaymentModel extends RepresentationModel<PaymentModel> {

		private final MonetaryAmount amount;
		private final CreditCardNumber creditCardNumber;
	}

	record PaymentForm(CreditCardNumber number) implements DTO {}
}
