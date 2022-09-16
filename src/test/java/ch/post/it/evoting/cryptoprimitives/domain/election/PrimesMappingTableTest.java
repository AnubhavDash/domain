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

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@DisplayName("A PrimesMappingTable")
class PrimesMappingTableTest {

	private static final String actualVotingOption = "actualVotingOption";
	private static final String otherActualVotingOption = "otherActualVotingOption";
	private static final GqGroup gqGroup = GroupTestData.getGroupP59();
	private static final int desiredNumberOfPrimes = 3;
	private static List<PrimesMappingTableEntry> primesMappingTableEntries;
	private static GroupVector<PrimeGqElement, GqGroup> smallPrimeGroupMembers;

	@BeforeAll
	static void setUpAll() {
		smallPrimeGroupMembers = PrimeGqElement.PrimeGqElementFactory.getSmallPrimeGroupMembers(gqGroup, desiredNumberOfPrimes + 1);
		primesMappingTableEntries = List.of(
				new PrimesMappingTableEntry(actualVotingOption, smallPrimeGroupMembers.get(0)),
				new PrimesMappingTableEntry(actualVotingOption, smallPrimeGroupMembers.get(1)),
				new PrimesMappingTableEntry(otherActualVotingOption, smallPrimeGroupMembers.get(2))
		);
	}

	@Test
	@DisplayName("from a null entry, throws a NullPointerException.")
	void addNullEntryThrows() {
		assertAll(() -> assertThrows(NullPointerException.class, () -> PrimesMappingTable.from(null)),
				() -> assertThrows(NullPointerException.class, () -> new PrimesMappingTable(null)));
	}

	@Test
	@DisplayName("from an empty entry, throws an IllegalArgumentException.")
	void addEmptyEntryThrows() {
		final List<PrimesMappingTableEntry> emptyList = List.of();
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> PrimesMappingTable.from(emptyList));

		assertEquals("The primes mapping table cannot be empty.", Throwables.getRootCause(illegalArgumentException).getMessage());

		final GroupVector<PrimesMappingTableEntry, GqGroup> emptyGroupVector = GroupVector.of();
		final IllegalArgumentException illegalArgumentException2 = assertThrows(IllegalArgumentException.class,
				() -> new PrimesMappingTable(emptyGroupVector));

		assertEquals("The primes mapping table cannot be empty.", Throwables.getRootCause(illegalArgumentException2).getMessage());
	}

	@Test
	@DisplayName("from a non-null entry, behaves as expected.")
	void addBehavesAsExpected() {
		assertEquals(desiredNumberOfPrimes, PrimesMappingTable.from(primesMappingTableEntries).size());
	}

	@Test
	@DisplayName("from an entries containing duplicated encoded voting options, throws an IllegalArgumentException.")
	void addAlreadyExistingEncodedVotingOptionEntryThrows() {

		final List<PrimesMappingTableEntry> duplicatedPrimesMappingTableEntries = List.of(primesMappingTableEntries.get(0),
				primesMappingTableEntries.get(0));

		final IllegalArgumentException illegalArgumentException =
				assertThrows(IllegalArgumentException.class, () -> PrimesMappingTable.from(duplicatedPrimesMappingTableEntries));

		assertEquals("The primes mapping table entries contain duplicated encoded voting options.",
				Throwables.getRootCause(illegalArgumentException).getMessage());
	}

	@Test
	@DisplayName("from valid entries, calling getPrimesMappingTableEntry with null input throws a NullPointerException.")
	void getPrimesMappingTableEntryNullInputThrows() {

		final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(primesMappingTableEntries);

		assertThrows(NullPointerException.class, () -> primesMappingTable.getPrimesMappingTableEntry(null));
	}

	@Test
	@DisplayName("from valid entries, calling getPrimesMappingTableEntry with an invalid input throws a NoSuchElementException.")
	void getPrimesMappingTableEntryInvalidInputThrows() {

		final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(primesMappingTableEntries);
		final PrimeGqElement encodedVotingOption = smallPrimeGroupMembers.get(desiredNumberOfPrimes);

		assertThrows(NoSuchElementException.class, () -> primesMappingTable.getPrimesMappingTableEntry(encodedVotingOption));
	}

	@Test
	@DisplayName("from valid entries, calling getPrimesMappingTableEntry with a valid input behaves as expected.")
	void getPrimesMappingTableEntryHappyPath() {

		final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(primesMappingTableEntries);
		final PrimeGqElement encodedVotingOption = smallPrimeGroupMembers.get(0);

		final PrimesMappingTableEntry primesMappingTableEntry =
				assertDoesNotThrow(() -> primesMappingTable.getPrimesMappingTableEntry(encodedVotingOption));

		assertEquals(encodedVotingOption, primesMappingTableEntry.encodedVotingOption());
		assertEquals(actualVotingOption, primesMappingTableEntry.actualVotingOption());
	}
}
