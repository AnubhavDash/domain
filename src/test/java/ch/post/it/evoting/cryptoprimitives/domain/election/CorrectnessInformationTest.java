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
package ch.post.it.evoting.cryptoprimitives.domain.election;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("A correctness information")
class CorrectnessInformationTest {
	private final static String CORRECTNESS_ID = "correctnessId";
	private final static Integer NUMBER_OF_SELECTIONS = 12;
	private final static Integer NUMBER_OF_VOTING_OPTIONS = 408;
	private final static List<BigInteger> LIST_OF_WRITE_IN_OPTIONS = List.of(BigInteger.ONE);

	@Test
	@DisplayName("constructed with a null parameter throws a NullPointerException.")
	void nullParameterTest() {
		assertAll(() -> assertThrows(NullPointerException.class,
						() -> new CorrectnessInformation(null, NUMBER_OF_SELECTIONS, NUMBER_OF_VOTING_OPTIONS, LIST_OF_WRITE_IN_OPTIONS)),
				() -> assertThrows(NullPointerException.class,
						() -> new CorrectnessInformation(CORRECTNESS_ID, null, NUMBER_OF_VOTING_OPTIONS, LIST_OF_WRITE_IN_OPTIONS)),
				() -> assertThrows(NullPointerException.class,
						() -> new CorrectnessInformation(CORRECTNESS_ID, NUMBER_OF_SELECTIONS, null, LIST_OF_WRITE_IN_OPTIONS)),
				() -> assertThrows(NullPointerException.class,
						() -> new CorrectnessInformation(CORRECTNESS_ID, NUMBER_OF_SELECTIONS, NUMBER_OF_VOTING_OPTIONS, null))
		);
	}

	@Test
	@DisplayName("constructed with a non strictly positive number of selections throws an IllegalArgumentException.")
	void nonStrictlyPositiveNumberOfSelectionsTest() {

		IllegalArgumentException zeroIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new CorrectnessInformation(CORRECTNESS_ID, 0, NUMBER_OF_VOTING_OPTIONS, LIST_OF_WRITE_IN_OPTIONS));

		assertEquals("The number of selections must be strictly positive.", zeroIllegalArgumentException.getMessage());

		IllegalArgumentException minusOneIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new CorrectnessInformation(CORRECTNESS_ID, -1, NUMBER_OF_VOTING_OPTIONS, LIST_OF_WRITE_IN_OPTIONS));

		assertEquals("The number of selections must be strictly positive.", minusOneIllegalArgumentException.getMessage());
	}

	@Test
	@DisplayName("constructed with a non strictly positive number of voting options throws an IllegalArgumentException.")
	void nonStrictlyPositiveNumberOfVotingOptionsTest() {

		IllegalArgumentException zeroIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new CorrectnessInformation(CORRECTNESS_ID, NUMBER_OF_SELECTIONS, 0, LIST_OF_WRITE_IN_OPTIONS));

		assertEquals("The number of voting options must be strictly positive.", zeroIllegalArgumentException.getMessage());

		IllegalArgumentException minusOneIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new CorrectnessInformation(CORRECTNESS_ID, NUMBER_OF_SELECTIONS, -1, LIST_OF_WRITE_IN_OPTIONS));

		assertEquals("The number of voting options must be strictly positive.", minusOneIllegalArgumentException.getMessage());
	}

	@Test
	@DisplayName("constructed with a number of selections bigger than the number of voting options throws an IllegalArgumentException.")
	void biggerNumberOfSelectionsTest() {

		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new CorrectnessInformation(CORRECTNESS_ID, NUMBER_OF_VOTING_OPTIONS + 1, NUMBER_OF_VOTING_OPTIONS, LIST_OF_WRITE_IN_OPTIONS));

		assertEquals("The number of selections must be at most the number of voting options.", illegalArgumentException.getMessage());
	}

	@Test
	@DisplayName("constructed with valid parameters does not throw any exception.")
	void validParametersTest() {
		assertDoesNotThrow(
				() -> new CorrectnessInformation(CORRECTNESS_ID, NUMBER_OF_SELECTIONS, NUMBER_OF_VOTING_OPTIONS, LIST_OF_WRITE_IN_OPTIONS));
	}

}
