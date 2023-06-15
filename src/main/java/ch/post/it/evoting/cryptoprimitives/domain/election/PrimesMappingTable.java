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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.MoreCollectors;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;

/**
 * Represents the primes mapping table - pTable = ((v<sub>0</sub>, p&#771;<sub>0</sub>),...,(v<sub>n-1</sub>, p&#771;<sub>n-1</sub>)) - an ordered
 * table of {@link PrimesMappingTableEntry} elements with
 * <ul>
 *     <li>Actual voting options v&#771; = (v<sub>0</sub>,...,v<sub>n-1</sub>)</li>
 *     <li>Encoded voting options p&#771; = (p&#771;<sub>0</sub>,...,p&#771;<sub>n-1</sub>)</li>
 * </ul>
 * This class is immutable.
 */
@JsonPropertyOrder({ "pTable" })
public class PrimesMappingTable implements HashableList {

	@JsonDeserialize(using = PrimesMappingTableEntryGroupVectorDeserializer.class)
	@JsonProperty
	private GroupVector<PrimesMappingTableEntry, GqGroup> pTable;

	@JsonCreator
	@VisibleForTesting
	public PrimesMappingTable(
			@JsonProperty("pTable")
			final GroupVector<PrimesMappingTableEntry, GqGroup> pTable) {
		this.pTable = checkNotNull(pTable);
		checkArgument(!this.pTable.isEmpty(), "The primes mapping table cannot be empty.");
		checkArgument(this.pTable.size() <= VotingOptionsConstants.MAXIMUM_NUMBER_OF_VOTING_OPTIONS,
				"The primes mapping table cannot have more than omega elements. [omega: %s]",
				VotingOptionsConstants.MAXIMUM_NUMBER_OF_VOTING_OPTIONS);
	}

	/**
	 * Builds a primes mapping table based on the given list of primes mapping table entries.
	 *
	 * @param primesMappingTableEntries the list of primes mapping table entries to be built from. Must be non-null, non-empty and its encoded voting
	 *                                  options must not contain any duplicate.
	 * @throws NullPointerException     if the primes mapping table entries is null.
	 * @throws IllegalArgumentException if the encoded voting options of the primes mapping table entries contain duplicates, or if the given list of
	 *                                  primes mapping table entries is empty or has more than
	 *                                  {@value VotingOptionsConstants#MAXIMUM_NUMBER_OF_VOTING_OPTIONS} elements.
	 */
	public static PrimesMappingTable from(final List<PrimesMappingTableEntry> primesMappingTableEntries) {
		final List<PrimesMappingTableEntry> primesMappingTableEntriesCopy = List.copyOf(checkNotNull(primesMappingTableEntries));

		final Set<PrimeGqElement> encodedVotingOptions = primesMappingTableEntriesCopy.stream()
				.map(PrimesMappingTableEntry::encodedVotingOption)
				.collect(Collectors.toSet());

		checkArgument(encodedVotingOptions.size() == primesMappingTableEntriesCopy.size(),
				"The primes mapping table entries contain duplicated encoded voting options.");

		return new PrimesMappingTable(GroupVector.from(primesMappingTableEntriesCopy));
	}

	/**
	 * Returns the number of elements in this primes mapping table.
	 *
	 * @return the number of elements in this primes mapping table.
	 */
	public int size() {
		return pTable.size();
	}

	/**
	 * Returns the i-th entry, of the primes mapping table - pTable - corresponding to the given encoded voting option.
	 *
	 * @param encodedVotingOption p&#771;<sub>i</sub>, the encoded voting option. Must be non-null.
	 * @return the i-th entry of the primes mapping table, (v<sub>i</sub>, p&#771; <sub>i</sub>).
	 * @throws NullPointerException   if the encoded voting option is null.
	 * @throws NoSuchElementException if no primes mapping table entry corresponds to the given encoded voting option.
	 */
	@JsonIgnore
	public PrimesMappingTableEntry getPrimesMappingTableEntry(final PrimeGqElement encodedVotingOption) {
		checkNotNull(encodedVotingOption);

		return pTable.stream()
				.filter(primesMappingTableEntry -> primesMappingTableEntry.encodedVotingOption().equals(encodedVotingOption))
				.collect(MoreCollectors.onlyElement());
	}

	@JsonIgnore
	public GroupVector<PrimesMappingTableEntry, GqGroup> getPTable() {
		return pTable;
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(pTable);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final PrimesMappingTable that = (PrimesMappingTable) o;
		return pTable.equals(that.pTable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pTable);
	}
}
