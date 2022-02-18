/*
 * Copyright 2022 Post CH Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.post.it.evoting.cryptoprimitives.domain.validations;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;

public final class UUIDValidations {

	@VisibleForTesting
	static final int UUID_LENGTH = 32;

	@VisibleForTesting
	static final String UUID_ALPHABET = "0123456789abcdef";

	private static final String UUID_REGEX = String.format("^[%s]{%d}$", UUID_ALPHABET, UUID_LENGTH);
	private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

	private UUIDValidations() {
		// Intentionally left blank.
	}

	/**
	 * Validates that the input string is a valid UUID according to the predefined pattern.
	 *
	 * @param toValidate the string to validate. Must be non-null.
	 * @throws NullPointerException      if the string is null.
	 * @throws FailedValidationException if the string validation fails.
	 */
	public static void validateUUID(final String toValidate) {
		checkNotNull(toValidate);

		if (!UUID_PATTERN.matcher(toValidate).matches()) {
			throw new FailedValidationException(
					String.format("The given string (%s) does not comply with the required UUID format (%s).", toValidate, UUID_REGEX));
		}
	}
}
