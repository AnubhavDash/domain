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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateBase64Encoded;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.HexFormat;
import java.util.Locale;

import ch.post.it.evoting.cryptoprimitives.math.Base64;
import ch.post.it.evoting.cryptoprimitives.math.BaseEncodingFactory;

/**
 * Conversion methods used during the serialization/deserialization of Mixnet payloads.
 */
public class ConversionUtils {

	private static final String HEX_PREFIX = "0x";
	private static final Base64 base64 = BaseEncodingFactory.createBase64();

	private ConversionUtils() {
		// Intentionally left blank.
	}

	/**
	 * Converts a positive {@link BigInteger} to its hexadecimal string representation. The string is prefixed with "0x".
	 *
	 * @param value the BigInteger to convert. Positive and not null.
	 * @return the hexadecimal string representation of {@code value}, prefixed with "0x".
	 */
	public static String bigIntegerToHex(final BigInteger value) {
		checkNotNull(value);
		checkArgument(value.signum() >= 0, "The BigInteger value must be positive. [sign: %s]", value.signum());

		// By convention, we ignore the meaning less leading zero.
		final String encoded = HexFormat.of().formatHex(value.toByteArray()).replaceFirst("^0+(?!$)(?=.)", "");

		return HEX_PREFIX + encoded.toUpperCase(Locale.ENGLISH);
	}

	/**
	 * Converts a positive {@link BigInteger} to its base64 string representation.
	 *
	 * @param value the BigInteger to convert. Positive and not null.
	 * @return the base64 string representation of {@code value}..
	 */
	public static String bigIntegerToBase64(final BigInteger value) {
		checkNotNull(value);
		checkArgument(value.signum() >= 0, "The BigInteger value must be positive. [sign: %s]", value.signum());

		return base64.base64Encode(value.toByteArray());
	}

	/**
	 * Converts the hexadecimal string representation of a BigInteger to a BigInteger. The string must be prefixed with "0x".
	 *
	 * @param hexString the string to convert. Not null.
	 * @return a BigInteger.
	 */
	public static BigInteger hexToBigInteger(final String hexString) {
		checkNotNull(hexString);
		checkArgument(hexString.length() > 2, "The provided string length must be at least 3.");
		checkArgument(HEX_PREFIX.equals(hexString.substring(0, 2)), String.format("The provided string must be prefixed with %s.", HEX_PREFIX));

		String encoded = hexString.substring(2);
		if (encoded.length() % 2 != 0) {
			encoded = "0" + encoded;
		}
		final byte[] intermediate = HexFormat.of().parseHex(encoded);

		return new BigInteger(BigInteger.ONE.signum(), intermediate);
	}

	/**
	 * Converts the base64 string representation of a BigInteger to a BigInteger. The string must be a valid base64 string.
	 *
	 * @param base64String the string to convert. Not null.
	 * @return a BigInteger.
	 */
	public static BigInteger base64ToBigInteger(final String base64String) {
		checkNotNull(base64String);
		validateBase64Encoded(base64String);

		final byte[] decodedBytes = base64.base64Decode(base64String);
		return new BigInteger(decodedBytes);
	}

}
