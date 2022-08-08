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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.UUID_LENGTH;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateBase32NoPadAlphabet;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateBase64Encoded;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;

class ValidationsTest {

	@DisplayName("Calling validateUUID with")
	@Nested
	class ValidateUUID {

		private static final Random random = RandomFactory.createRandom();

		@DisplayName("a null input string throws a NullPointerException.")
		@Test
		void nullStringThrows() {
			assertThrows(NullPointerException.class, () -> validateUUID(null));
		}

		@DisplayName("an invalid input string throws a FailedValidationException.")
		@Test
		void invalidStringThrows() {
			assertThrows(FailedValidationException.class, () -> validateUUID("toValidate"));
		}

		@DisplayName("a valid input string does not throw.")
		@Test
		void validStringDoesNotThrow() {
			assertDoesNotThrow(() -> validateUUID(random.genRandomBase16String(UUID_LENGTH).toLowerCase()));
		}
	}

	@DisplayName("Calling validateBase64Encoded with")
	@Nested
	class ValidateBase64 {

		@DisplayName("a null input string throws a NullPointerException.")
		@Test
		void nullStringThrows() {
			assertThrows(NullPointerException.class, () -> validateBase64Encoded(null));
		}

		@DisplayName("an invalid input string throws a FailedValidationException.")
		@Test
		void invalidStringThrows() {
			final String invalidString = "$$";
			assertThrows(FailedValidationException.class, () -> validateBase64Encoded(invalidString));
		}

		@DisplayName("a valid input string does not throw.")
		@Test
		void validStringDoesNotThrow() {
			final String validString = "QmFzZTY0RXhhbXBsZVN0cmluZw==";
			assertDoesNotThrow(() -> validateBase64Encoded(validString));
		}
	}

	@DisplayName("Calling validateBase32NoPadAlphabet with")
	@Nested
	class ValidateBase32WithNoPad {
		private final int LENGTH = 24;
		private final String validString = "";

		@DisplayName("a null input string throws a NullPointerException.")
		@Test
		void nullStringThrows() {
			assertThrows(NullPointerException.class, () -> validateBase32NoPadAlphabet(null, LENGTH));
		}

		@DisplayName("a non strictly positive length throws an IllegalArgumentException.")
		@Test
		void invalidLengthThrows() {
			assertThrows(IllegalArgumentException.class, () -> validateBase32NoPadAlphabet(validString, -1));
			assertThrows(IllegalArgumentException.class, () -> validateBase32NoPadAlphabet(validString, 0));
		}

		@DisplayName("an input string without the expected length throws an IllegalArgumentException.")
		@Test
		void invalidLengthStringThrows() {
			final String invalidString = "invalidString";
			assertThrows(IllegalArgumentException.class, () -> validateBase32NoPadAlphabet(invalidString, LENGTH));
		}

		@DisplayName("an invalid input string throws a FailedValidationException.")
		@Test
		void invalidStringThrows() {
			final String invalidString = "invalidString";
			final int expectedLength = invalidString.length();
			assertThrows(FailedValidationException.class, () -> validateBase32NoPadAlphabet(invalidString, expectedLength));
		}

		@DisplayName("a valid input string does not throw.")
		@Test
		void validStringDoesNotThrow() {
			final String validString = "KRUGS42JONAUEYLTMUZTELRO".toLowerCase();
			assertDoesNotThrow(() -> validateBase32NoPadAlphabet(validString, LENGTH));
		}
	}
}
