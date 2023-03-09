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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public final class Validations {

	@VisibleForTesting
	static final int UUID_LENGTH = 32;

	private static final String BASE16_ALPHABET_WITH_LOWERCASE = "0123456789abcdefABCDEF";
	private static final String BASE32_LOWERCASE_NO_PAD_ALPHABET = "abcdefghijklmnopqrstuvwxyz234567";

	private static final String UUID_REGEX = String.format("^[%s]{%d}$", BASE16_ALPHABET_WITH_LOWERCASE, UUID_LENGTH);
	private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

	private Validations() {
		// Intentionally left blank.
	}

	/**
	 * Checks the input has no duplicates.
	 *
	 * @param toValidate the collection to validate. Must be non-null.
	 * @return true if the input does not have duplicates, false otherwise.
	 * @throws NullPointerException      if the collection is null or contains any null elements.
	 */
	public static boolean hasNoDuplicates(final Collection<?> toValidate) {
		checkNotNull(toValidate);
		toValidate.forEach(Preconditions::checkNotNull);

		return new HashSet<>(toValidate).size() == toValidate.size();
	}

	/**
	 * Validates that the input string is in Base16 alphabet ({@value BASE16_ALPHABET_WITH_LOWERCASE}) and has length {@value UUID_LENGTH}.
	 * <p>
	 *     The validation allows for both lowercase and uppercase input.
	 * </p>
	 *
	 * @param toValidate the string to validate. Must be non-null.
	 * @return the validated input string.
	 * @throws NullPointerException      if the string is null.
	 * @throws FailedValidationException if the string validation fails.
	 */
	public static String validateUUID(final String toValidate) {
		checkNotNull(toValidate);
		return validateInAlphabet(toValidate, UUID_PATTERN);
	}

	/**
	 * Validates that the input string is in Base32 lowercase alphabet with no pad ({@value BASE32_LOWERCASE_NO_PAD_ALPHABET}) and has the given
	 * expected length.
	 *
	 * @param toValidate     the string to validate. Must be non-null.
	 * @param expectedLength the expected length of the string to validate. Must be strictly positive.
	 * @return the validated input string.
	 * @throws NullPointerException      if the string is null.
	 * @throws IllegalArgumentException  if the expected length is not strictly positive or if the string is not of expected lentgh.
	 * @throws FailedValidationException if the string validation fails.
	 */
	public static String validateBase32NoPadAlphabet(final String toValidate, final int expectedLength) {
		checkNotNull(toValidate);
		checkArgument(expectedLength > 0, "The length must be strictly positive. [length: %s]", expectedLength);
		checkArgument(toValidate.length() == expectedLength, "The given string is not of expected length. [string: %s, expected length: %s]",
				toValidate, expectedLength);
		final String regex = String.format("^[%s]{%d}$", BASE32_LOWERCASE_NO_PAD_ALPHABET, expectedLength);
		return validateInAlphabet(toValidate, Pattern.compile(regex));
	}

	/**
	 * Validates that the input string is a valid Base64 encoded string.
	 *
	 * @param toValidate the string to validate. Must be non-null.
	 * @return the validated input string.
	 * @throws NullPointerException      if the string is null.
	 * @throws FailedValidationException if the string validation fails.
	 */
	public static String validateBase64Encoded(final String toValidate) {
		checkNotNull(toValidate);

		final Base64.Decoder decoder = Base64.getDecoder();
		try {
			decoder.decode(toValidate.getBytes(StandardCharsets.UTF_8));
		} catch (final IllegalArgumentException e) {
			throw new FailedValidationException(
					String.format("The given string is not a valid Base64 encoded string. [string: %s].", toValidate));
		}
		return toValidate;
	}

	private static String validateInAlphabet(final String toValidate, final Pattern pattern) {
		checkNotNull(toValidate);
		checkNotNull(pattern);

		if (!pattern.matcher(toValidate).matches()) {
			throw new FailedValidationException(
					String.format("The given string does not comply with the required format. [string: %s, format: %s].", toValidate,
							pattern.pattern()));
		}
		return toValidate;
	}

}
