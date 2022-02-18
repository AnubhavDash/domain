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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

/**
 * Conversion methods used during the serialization/deserialization of Mixnet payloads.
 */
public class ConversionUtils {

	private static final String HEX_PREFIX = "0x";

	private ConversionUtils() {
		// Intentionally left blank.
	}

	/**
	 * Converts a {@link BigInteger} to its hexadecimal string representation. The string is prefixed with "0x".
	 *
	 * @param value the BigInteger to convert. Not null.
	 * @return the hexadecimal string representation of {@code value}, prefixed with "0x".
	 */
	public static String bigIntegerToHex(final BigInteger value) {
		checkNotNull(value);

		return HEX_PREFIX + value.toString(16).toUpperCase();
	}

	/**
	 * Converts the hexadecimal string representation of a BigInteger to a BigInteger. The string must be prefixed with "0x".
	 *
	 * @param hexString the string to convert. Not null.
	 * @return a BigInteger.
	 */
	public static BigInteger hexToBigInteger(final String hexString) {
		checkNotNull(hexString);
		checkArgument(HEX_PREFIX.equals(hexString.substring(0, 2)), String.format("The provided string must be prefixed with %s.", HEX_PREFIX));

		return new BigInteger(hexString.substring(2), 16);
	}

}
