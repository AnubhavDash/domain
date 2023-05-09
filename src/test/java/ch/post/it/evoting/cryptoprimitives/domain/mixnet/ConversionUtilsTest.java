package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

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


	@RepeatedTest(100)
	void cyclicConversion() {
		final BigInteger asBigInt = random.genRandomInteger(BigInteger.ONE.shiftLeft(3072));

		final String asHex = ConversionUtils.bigIntegerToHex(asBigInt);
		final BigInteger cyclic = ConversionUtils.hexToBigInteger(asHex);

		assertEquals(asBigInt, cyclic);
	}
}