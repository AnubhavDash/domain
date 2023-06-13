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
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDateFromDateTo;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultDescription;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultTitle;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateGracePeriod;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateStatus;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates the information contained within a ballot box.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BallotBox(String id,
						String defaultTitle,
						String defaultDescription,
						String alias,
						LocalDateTime dateFrom,
						LocalDateTime dateTo,
						boolean test,
						String status,
						Integer gracePeriod,
						Identifier electionEvent,
						Identifier ballot,
						Identifier electoralBoard) {

	public BallotBox {
		validateUUID(id);
		validateDefaultTitle(defaultTitle);
		validateDefaultDescription(defaultDescription);
		validateAlias(alias);
		validateDateFromDateTo(dateFrom, dateTo);
		validateStatus(status);
		validateGracePeriod(gracePeriod);
		checkNotNull(electionEvent);
		checkNotNull(ballot);
		checkNotNull(electoralBoard);
	}

	@JsonGetter("test")
	public String getTest() {
		return String.valueOf(test);
	}

	@JsonGetter("gracePeriod")
	public String getGracePeriod() {
		return String.valueOf(gracePeriod);
	}

	public static final class BallotBoxBuilder {
		private String id;
		private String defaultTitle;
		private String defaultDescription;
		private String alias;
		private LocalDateTime dateFrom;
		private LocalDateTime dateTo;
		private boolean test;
		private String status;
		private Integer gracePeriod;
		private Identifier electionEvent;
		private Identifier ballot;
		private Identifier electoralBoard;

		public BallotBoxBuilder setId(final String id) {
			this.id = id;
			return this;
		}

		public BallotBoxBuilder setDefaultTitle(final String defaultTitle) {
			this.defaultTitle = defaultTitle;
			return this;
		}

		public BallotBoxBuilder setDefaultDescription(final String defaultDescription) {
			this.defaultDescription = defaultDescription;
			return this;
		}

		public BallotBoxBuilder setAlias(final String alias) {
			this.alias = alias;
			return this;
		}

		public BallotBoxBuilder setDateFrom(final LocalDateTime dateFrom) {
			this.dateFrom = dateFrom;
			return this;
		}

		public BallotBoxBuilder setDateTo(final LocalDateTime dateTo) {
			this.dateTo = dateTo;
			return this;
		}

		public BallotBoxBuilder setTest(final boolean test) {
			this.test = test;
			return this;
		}

		public BallotBoxBuilder setStatus(final String status) {
			this.status = status;
			return this;
		}

		public BallotBoxBuilder setGracePeriod(final Integer gracePeriod) {
			this.gracePeriod = gracePeriod;
			return this;
		}

		public BallotBoxBuilder setElectionEvent(final Identifier electionEvent) {
			this.electionEvent = electionEvent;
			return this;
		}

		public BallotBoxBuilder setBallot(final Identifier ballot) {
			this.ballot = ballot;
			return this;
		}

		public BallotBoxBuilder setElectoralBoard(final Identifier electoralBoard) {
			this.electoralBoard = electoralBoard;
			return this;
		}

		public BallotBox build() {
			return new BallotBox(id, defaultTitle, defaultDescription, alias, dateFrom, dateTo, test, status, gracePeriod, electionEvent, ballot,
					electoralBoard);
		}
	}
}
