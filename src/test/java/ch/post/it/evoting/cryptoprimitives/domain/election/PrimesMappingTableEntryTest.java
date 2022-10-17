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

import static ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants.MAXIMUM_ACTUAL_VOTING_OPTION_LENGTH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;

@DisplayName("A PrimesMappingTableEntry")
class PrimesMappingTableEntryTest extends TestGroupSetup {

	private static final String actualVotingOption = "b9c57a40-a555-35e1-972c-a1a6b7e03381";
	private static PrimeGqElement encodedVotingOption;

	@BeforeAll
	static void setUpAll() {
		encodedVotingOption = gqGroupGenerator.genSmallPrimeMember();
	}

	@Test
	@DisplayName("created with a null actual voting option, throws a NullPointerException.")
	void nullActualVotingOption() {
		assertThrows(NullPointerException.class, () -> new PrimesMappingTableEntry(null, encodedVotingOption));
	}

	@Test
	@DisplayName("created with a null encoded voting option, throws a NullPointerException.")
	void nullEncodedVotingOption() {
		assertThrows(NullPointerException.class, () -> new PrimesMappingTableEntry(actualVotingOption, null));
	}

	@Test
	@DisplayName("created with an empty actual voting option, throws an IllegalArgumentException.")
	void emptyActualVotingOption() {
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new PrimesMappingTableEntry("", encodedVotingOption));

		assertEquals("The actual voting option cannot be blank.", Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("created with a blank actual voting option, throws an IllegalArgumentException.")
	void blankActualVotingOption() {
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new PrimesMappingTableEntry(" ", encodedVotingOption));

		assertEquals("The actual voting option cannot be blank.", Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("created with an actual voting option too large, throws an IllegalArgumentException.")
	void largeActualVotingOption() {
		final String largeActualVotingOption = "8KU8zJ0OR0ZDgLTDopHrVRAg4c55w8gqdjHPYWx6kh8CIkRQNbF";

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new PrimesMappingTableEntry(largeActualVotingOption, encodedVotingOption));

		assertEquals(String.format("The actual voting option length must not exceed %s. [length: %s]",
						MAXIMUM_ACTUAL_VOTING_OPTION_LENGTH, largeActualVotingOption.length()),
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("created with a non-null values does not throw.")
	void withNonNullValuesDoesNotThrow() {
		assertDoesNotThrow(() -> new PrimesMappingTableEntry(actualVotingOption, encodedVotingOption));
	}

	@ParameterizedTest
	@DisplayName("Using invalid actualVotingOptions, throws an IllegalArgumentException.")
	@ValueSource(strings = {" a  b", "abc  ", "apéosidvnbq13458zœ¶@¼←“þ“ ¢@]œ“→”@µ€ĸ@{þ", "<ycx", " sfasdfa", "pk23]", "asdacà!32", " a p2o3m", "<xyz>" })
	void invalidXMLTokenForActualVotingOptions(String actualVotingOption) {
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new PrimesMappingTableEntry(actualVotingOption, encodedVotingOption));

		assertEquals(String.format("Voting options should match a valid xml xs:token [actualVotingOption: %s] ",
				actualVotingOption), Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@ParameterizedTest
	@DisplayName("Using valid actualVotingOptions.")
	@ValueSource(strings = {"aposidvnbq13458z", "ab" , "vaner82", "xyz", "a_token", "another-token"})
	void validXMLTokenForActualVotingOptions(String actualVotingOption) {
		assertDoesNotThrow(()->new PrimesMappingTableEntry(actualVotingOption, encodedVotingOption));
	}

	@Test
	void invalidXMLTokenLengthForActualVotingOptions() {
		String fiftyOne = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXY";
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new PrimesMappingTableEntry(fiftyOne, encodedVotingOption));

		assertEquals(String.format("The actual voting option length must not exceed 50. [length: %d]",
				fiftyOne.length()), Throwables.getRootCause(illegalArgumentException).getMessage());
	}
}
