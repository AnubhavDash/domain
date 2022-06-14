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

import java.math.BigInteger;
import java.util.List;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

public record CorrectnessInformation(
		String correctnessId,
		Integer numberOfSelections,
		Integer numberOfVotingOptions) implements HashableList {

	/**
	 * The constructor.
	 * <p>
	 * Must respect the following:
	 * <ul>
	 * 	<li>the number of selections must be at most the number of voting options.</li>
	 * </ul>
	 *
	 * @param correctnessId         The correctness id. Must be non-null.
	 * @param numberOfSelections    The number of selections. Must be non-null and strictly positive.
	 * @param numberOfVotingOptions The number of voting options. Must be non-null and strictly positive.
	 */
	public CorrectnessInformation {
		checkNotNull(correctnessId);

		checkNotNull(numberOfSelections);
		checkArgument(numberOfSelections > 0, "The number of selections must be strictly positive.");

		checkNotNull(numberOfVotingOptions);
		checkArgument(numberOfVotingOptions > 0, "The number of voting options must be strictly positive.");

		checkArgument(numberOfSelections <= numberOfVotingOptions, "The number of selections must be at most the number of voting options.");
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(
				HashableString.from(correctnessId),
				HashableBigInteger.from(BigInteger.valueOf(numberOfSelections)),
				HashableBigInteger.from(BigInteger.valueOf(numberOfVotingOptions)));
	}
}
