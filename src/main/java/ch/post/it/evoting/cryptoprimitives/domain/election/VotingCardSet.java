/*
 * Copyright 2023 Post CH Ltd
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

import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateAlias;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultDescriptionWithPrefix;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultTitleWithPrefix;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateStatus;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates the information contained within a voting card set.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VotingCardSet(String id,
							String defaultTitle,
							String defaultDescription,
							String alias,
							Integer numberOfVotingCardsToGenerate,
							String verificationCardSetId,
							String status,
							Identifier electionEvent,
							Identifier ballotBox) {

	@JsonIgnore
	public static final String PREFIX = "vcs_";

	public VotingCardSet {
		validateUUID(id);
		validateDefaultTitleWithPrefix(defaultTitle, PREFIX);
		validateDefaultDescriptionWithPrefix(defaultDescription, PREFIX);
		validateAlias(alias);
		validateUUID(verificationCardSetId);
		validateStatus(status);
		checkNotNull(electionEvent);
		checkNotNull(ballotBox);

		checkArgument(numberOfVotingCardsToGenerate > 0,
				"The number of voting cards to generate must be strictly positive. [numberOfVotingCardsToGenerate: %s, votingCardSetId: %s]",
				numberOfVotingCardsToGenerate, id);
	}

	public static final class VotingCardSetBuilder {
		private String id;
		private String defaultTitle;
		private String defaultDescription;
		private String alias;
		private Integer numberOfVotingCardsToGenerate;
		private String verificationCardSetId;
		private String status;
		private Identifier electionEvent;
		private Identifier ballotBox;

		public VotingCardSetBuilder setId(final String id) {
			this.id = id;
			return this;
		}

		public VotingCardSetBuilder setDefaultTitle(final String defaultTitle) {
			this.defaultTitle = defaultTitle;
			return this;
		}

		public VotingCardSetBuilder setDefaultDescription(final String defaultDescription) {
			this.defaultDescription = defaultDescription;
			return this;
		}

		public VotingCardSetBuilder setAlias(final String alias) {
			this.alias = alias;
			return this;
		}

		public VotingCardSetBuilder setNumberOfVotingCardsToGenerate(final Integer numberOfVotingCardsToGenerate) {
			this.numberOfVotingCardsToGenerate = numberOfVotingCardsToGenerate;
			return this;
		}

		public VotingCardSetBuilder setVerificationCardSetId(final String verificationCardSetId) {
			this.verificationCardSetId = verificationCardSetId;
			return this;
		}

		public VotingCardSetBuilder setStatus(final String status) {
			this.status = status;
			return this;
		}

		public VotingCardSetBuilder setElectionEvent(final Identifier electionEvent) {
			this.electionEvent = electionEvent;
			return this;
		}

		public VotingCardSetBuilder setBallotBox(final Identifier ballotBox) {
			this.ballotBox = ballotBox;
			return this;
		}

		public VotingCardSet build() {
			return new VotingCardSet(id, defaultTitle, defaultDescription, alias, numberOfVotingCardsToGenerate, verificationCardSetId, status,
					electionEvent, ballotBox);
		}
	}
}
