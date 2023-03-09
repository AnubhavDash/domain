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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.hasNoDuplicates;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

public record ElectionEventContext(String electionEventId,
								   List<VerificationCardSetContext> verificationCardSetContexts,
								   LocalDateTime startTime,
								   LocalDateTime finishTime) implements HashableList {

	public ElectionEventContext(final String electionEventId,
			final List<VerificationCardSetContext> verificationCardSetContexts,
			final LocalDateTime startTime,
			final LocalDateTime finishTime) {

		this.electionEventId = validateUUID(electionEventId);
		this.verificationCardSetContexts = List.copyOf(checkNotNull(verificationCardSetContexts));
		this.startTime = checkNotNull(startTime);
		this.finishTime = checkNotNull(finishTime);

		this.verificationCardSetContexts.forEach(Preconditions::checkNotNull);

		checkArgument(!this.verificationCardSetContexts.isEmpty(), "VerificationCardSetContexts cannot be empty.");
		checkArgument(hasNoDuplicates(this.verificationCardSetContexts.stream()
				.map(VerificationCardSetContext::ballotBoxId)
				.toList()), "VerificationCardSetContexts cannot contain duplicate BallotBoxIds.");
		checkArgument(hasNoDuplicates(this.verificationCardSetContexts.stream()
				.map(VerificationCardSetContext::verificationCardSetId)
				.toList()), "VerificationCardSetContexts cannot contain duplicate VerificationCardSetIds.");
		checkArgument(this.verificationCardSetContexts.stream()
						.map(VerificationCardSetContext::numberOfWriteInFields)
						.allMatch(n -> n >= 0),
				"VerificationCardSetContexts cannot contain negative numberOfWriteInFields.");
	}

	/**
	 * @return the maximum number of write-in fields in all verification card sets.
	 */
	@JsonIgnore
	public int getMaxNumberOfWriteInFields() {
		return this.verificationCardSetContexts.stream()
				.max(Comparator.comparingInt(VerificationCardSetContext::numberOfWriteInFields))
				.orElseThrow(IllegalArgumentException::new) // Guaranteed to never occur by the constructor checkNotNull.
				.numberOfWriteInFields();
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(HashableString.from(electionEventId),
				HashableList.from(verificationCardSetContexts),
				HashableString.from(startTime.toString()),
				HashableString.from(finishTime.toString()));
	}
}