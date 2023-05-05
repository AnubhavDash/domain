package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;

class ConversionUtilsTest {

	private static final Random random = RandomFactory.createRandom();

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

	@RepeatedTest(100)
	void cyclicConversion() {
		final BigInteger asBigInt = random.genRandomInteger(BigInteger.ONE.shiftLeft(3072));

		final String asHex = ConversionUtils.bigIntegerToHex(asBigInt);
		final BigInteger cyclic = ConversionUtils.hexToBigInteger(asHex);

		assertEquals(asBigInt, cyclic);
	}
}