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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVectorElement;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;

/**
 * Represents an entry, say the i-th entry, of the primes mapping table - pTable.
 *
 * @param actualVotingOption  v<sub>i</sub>, the actual voting option. Must be non-null. It must not be blank and its length must be at most
 *                            {@value ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants#MAXIMUM_ACTUAL_VOTING_OPTION_LENGTH}.
 * @param encodedVotingOption p&#771;<sub>i</sub>, the encoded voting option. Must be non-null.
 */
public record PrimesMappingTableEntry(String actualVotingOption, PrimeGqElement encodedVotingOption)
		implements GroupVectorElement<GqGroup>, HashableList {

	public PrimesMappingTableEntry {
		checkNotNull(actualVotingOption);
		checkNotNull(encodedVotingOption);

		checkArgument(!actualVotingOption.isBlank(), "The actual voting option cannot be blank.");
		checkArgument(actualVotingOption.length() <= MAXIMUM_ACTUAL_VOTING_OPTION_LENGTH,
				"The actual voting option length must not exceed %s. [length: %s]", MAXIMUM_ACTUAL_VOTING_OPTION_LENGTH,
				actualVotingOption.length());
	}

	@JsonIgnore
	@Override
	public GqGroup getGroup() {
		return encodedVotingOption.getGroup();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(
				HashableString.from(actualVotingOption),
				HashableBigInteger.from(encodedVotingOption.getValue()));
	}
}
