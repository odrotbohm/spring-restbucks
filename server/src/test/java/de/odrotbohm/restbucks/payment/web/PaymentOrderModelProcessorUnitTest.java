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

import static de.odrotbohm.restbucks.order.Order.Status.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.odrotbohm.restbucks.order.Order;
import de.odrotbohm.restbucks.order.OrderTestUtils;
import de.odrotbohm.restbucks.order.Order.Status;
import de.odrotbohm.restbucks.payment.web.PaymentLinks;
import de.odrotbohm.restbucks.payment.web.PaymentOrderModelProcessor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Unit test for {@link PaymentOrderModelProcessorUnitTest}.
 *
 * @author Oliver Gierke
 */
@ExtendWith(MockitoExtension.class)
class PaymentOrderModelProcessorUnitTest {

	@Mock(lenient = true) //
	PaymentLinks paymentLinks;

	PaymentOrderModelProcessor processor;
	Link paymentLink, receiptLink;

	@BeforeEach
	void setUp() {

		HttpServletRequest request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);

		paymentLink = Link.of("payment", PaymentLinks.PAYMENT_REL);
		receiptLink = Link.of("receipt", PaymentLinks.RECEIPT_REL);

		processor = new PaymentOrderModelProcessor(paymentLinks);
		when(paymentLinks.getPaymentLink(Mockito.any(Order.class))).thenReturn(paymentLink);
		when(paymentLinks.getReceiptLink(Mockito.any(Order.class))).thenReturn(receiptLink);
	}

	@Test
	void doesNotAddLinksForNeitherFreshNorUnfinishedOrders() {

		for (Status status : Status.values()) {

			if (status == READY || status == PAYMENT_EXPECTED) {
				continue;
			}

			Order order = OrderTestUtils.createExistingOrderWithStatus(status);
			EntityModel<Order> resource = processor.process(EntityModel.of(order));

			assertThat(resource.hasLinks()).isFalse();
		}
	}

	@Test
	void addsPaymentLinkForFreshOrder() {

		Order order = OrderTestUtils.createExistingOrder();

		EntityModel<Order> resource = processor.process(EntityModel.of(order));
		assertThat(resource.getLink(PaymentLinks.PAYMENT_REL)).hasValue(paymentLink);
	}

	@Test
	void addsReceiptLinkForPaidOrder() {

		Order order = OrderTestUtils.createPreparedOrder();

		EntityModel<Order> resource = processor.process(EntityModel.of(order));
		assertThat(resource.getLink(PaymentLinks.RECEIPT_REL))
				.map(it -> it.withAffordances(Collections.emptyList()))
				.hasValue(receiptLink);
	}
}
