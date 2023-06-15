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
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateNonBlankUCS;
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
								   String electionEventAlias,
								   String electionEventDescription,
								   List<VerificationCardSetContext> verificationCardSetContexts,
								   LocalDateTime startTime,
								   LocalDateTime finishTime) implements HashableList {

	public ElectionEventContext(final String electionEventId,
			final String electionEventAlias,
			final String electionEventDescription,
			final List<VerificationCardSetContext> verificationCardSetContexts,
			final LocalDateTime startTime,
			final LocalDateTime finishTime) {

		this.electionEventId = validateUUID(electionEventId);
		this.electionEventAlias = validateNonBlankUCS(electionEventAlias);
		this.electionEventDescription = validateNonBlankUCS(electionEventDescription);
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
		checkArgument(hasNoDuplicates(this.verificationCardSetContexts.stream()
				.map(VerificationCardSetContext::verificationCardSetAlias)
				.toList()), "VerificationCardSetContexts cannot contain duplicate VerificationCardSetAliases.");
		checkArgument(startTime.isBefore(finishTime) || startTime.equals(finishTime), "The start time must not be after the finish time.");
		checkArgument(this.verificationCardSetContexts.stream()
						.map(VerificationCardSetContext::ballotBoxStartTime)
						.allMatch(ballotBoxStartTime -> (startTime.isBefore(ballotBoxStartTime) || startTime.equals(ballotBoxStartTime))
								&& (ballotBoxStartTime.isBefore(finishTime) || ballotBoxStartTime.equals(finishTime))),
				"The ballot box start times must be between the election event start and finish times.");
		checkArgument(this.verificationCardSetContexts.stream()
						.map(VerificationCardSetContext::ballotBoxFinishTime)
						.allMatch(ballotBoxFinishTime -> (startTime.isBefore(ballotBoxFinishTime) || startTime.equals(ballotBoxFinishTime))
								&& (ballotBoxFinishTime.isBefore(finishTime) || ballotBoxFinishTime.equals(finishTime))),
				"The ballot box finish times must be between the election event start and finish times.");
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
