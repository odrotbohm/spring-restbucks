package org.springsource.restbucks.order.web;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class MethodNotAllowedBecauseOfCurrentStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MethodNotAllowedBecauseOfCurrentStateException(MismatchingMessageCorrelationException ex) {
		super(ex);
	}

}
