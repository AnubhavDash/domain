/*
 * Copyright 2023 Post CH Ltd
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

package ch.post.it.evoting.cryptoprimitives.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.domain.validations.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;

class ConversionUtilsTest {

	private static final Random random = RandomFactory.createRandom();

	@Nested
	@DisplayName("Integer to Hex")
	class IntegerToHex {

		@Test
		void trimsLeadingZeroes() {
			final BigInteger bigInteger = BigInteger.valueOf(7L);
			final String asHex = ConversionUtils.bigIntegerToHex(bigInteger);

			assertEquals("0x7", asHex);
		}

		@Test
		void noTrimmingForZero() {
			final String asHex = ConversionUtils.bigIntegerToHex(BigInteger.ZERO);

			assertEquals("0x0", asHex);
		}

		@Test
		void negativeValueThrows() {
			final BigInteger negativeValue = BigInteger.ONE.negate();
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> ConversionUtils.bigIntegerToHex(negativeValue));
			assertEquals("The BigInteger value must be positive. [sign: -1]", illegalArgumentException.getMessage());
		}

		@Test
		void nullValueThrows() {
			assertThrows(NullPointerException.class, () -> ConversionUtils.bigIntegerToHex(null));
		}

	}

	@Nested
	@DisplayName("Integer to Base64")
	class IntegerToBase64 {

		@Test
		void negativeValueThrows() {
			final BigInteger negativeValue = BigInteger.ONE.negate();
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> ConversionUtils.bigIntegerToBase64(negativeValue));
			assertEquals("The BigInteger value must be positive. [sign: -1]", illegalArgumentException.getMessage());
		}

		@Test
		void nullValueThrows() {
			assertThrows(NullPointerException.class, () -> ConversionUtils.bigIntegerToBase64(null));
		}

	}

	@Nested
	@DisplayName("Hex to Integer")
	class HexToInteger {

		@Test
		void nullValueThrows() {
			assertThrows(NullPointerException.class, () -> ConversionUtils.hexToBigInteger(null));
		}

		@Test
		void tooShortValueThrows() {
			final String notPrefixedValue = "0x";
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> ConversionUtils.hexToBigInteger(notPrefixedValue));
			assertEquals("The provided string length must be at least 3.", illegalArgumentException.getMessage());
		}

		@Test
		void notPrefixedValueThrows() {
			final String notPrefixedValue = "15F";
			final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> ConversionUtils.hexToBigInteger(notPrefixedValue));
			assertEquals("The provided string must be prefixed with 0x.", illegalArgumentException.getMessage());
		}

	}

	@Nested
	@DisplayName("Base64 to Integer")
	class Base64ToInteger {

		@Test
		void nullValueThrows() {
			assertThrows(NullPointerException.class, () -> ConversionUtils.base64ToBigInteger(null));
		}

		@Test
		void tooShortValueThrows() {
			final String tooShortValue = "Bw=";
			final FailedValidationException failedValidationException = assertThrows(FailedValidationException.class,
					() -> ConversionUtils.base64ToBigInteger(tooShortValue));
			assertEquals("The given string is not a valid Base64 encoded string. [string: Bw=].", failedValidationException.getMessage());
		}

	}

	@RepeatedTest(100)
	void cyclicHexConversion() {
		final BigInteger asBigInt = random.genRandomInteger(BigInteger.ONE.shiftLeft(3072));

		final String asHex = ConversionUtils.bigIntegerToHex(asBigInt);
		final BigInteger cyclic = ConversionUtils.hexToBigInteger(asHex);

		assertEquals(asBigInt, cyclic);
	}

	@RepeatedTest(100)
	void cyclicBase64Conversion() {
		final BigInteger asBigInt = random.genRandomInteger(BigInteger.ONE.shiftLeft(3072));

		final String asBase64 = ConversionUtils.bigIntegerToBase64(asBigInt);
		final BigInteger cyclic = ConversionUtils.base64ToBigInteger(asBase64);

		assertEquals(asBigInt, cyclic);
	}
}