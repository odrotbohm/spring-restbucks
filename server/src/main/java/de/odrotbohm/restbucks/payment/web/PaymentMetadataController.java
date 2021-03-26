package de.odrotbohm.restbucks.payment.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

// @Controller
class PaymentMetadataController {

	@GetMapping(path = "/docs/payment", produces = MediaTypes.HAL_FORMS_JSON_VALUE)
	ResponseEntity<RepresentationModel<?>> getPaymentMetadata() {

		Link selfLink = linkTo(methodOn(PaymentMetadataController.class).getPaymentMetadata()).withSelfRel()
				.andAffordance(afford(methodOn(PaymentController.class).submitPayment(null, null)));

		return ResponseEntity.ok(new RepresentationModel<>(selfLink));
	}
}
