/*
 * Copyright 2022 by Swiss Post, Information Technology
 */

package ch.post.it.evoting.cryptoprimitives.domain.validations;

/**
 * Thrown to indicate that a validation failed.
 */
public class FailedValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FailedValidationException(String message) {
		super(message);
	}
}
