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
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateBoardMembers;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultDescription;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultTitle;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateStatus;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates the information contained within an electoral authority.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ElectoralBoard(String id,
							 String defaultTitle,
							 String defaultDescription,
							 String alias,
							 String status,
							 List<String> boardMembers,
							 Identifier electionEvent) {

	public ElectoralBoard {
		validateUUID(id);
		validateDefaultTitle(defaultTitle);
		validateDefaultDescription(defaultDescription);
		validateAlias(alias);
		validateStatus(status);
		validateBoardMembers(boardMembers);
		checkNotNull(electionEvent);
	}

	public static final class ElectoralBoardBuilder {
		private String id;
		private String defaultTitle;
		private String defaultDescription;
		private String alias;
		private String status;
		private List<String> boardMembers;
		private Identifier electionEvent;

		public ElectoralBoardBuilder setId(final String id) {
			this.id = id;
			return this;
		}

		public ElectoralBoardBuilder setDefaultTitle(final String defaultTitle) {
			this.defaultTitle = defaultTitle;
			return this;
		}

		public ElectoralBoardBuilder setDefaultDescription(final String defaultDescription) {
			this.defaultDescription = defaultDescription;
			return this;
		}

		public ElectoralBoardBuilder setAlias(final String alias) {
			this.alias = alias;
			return this;
		}

		public ElectoralBoardBuilder setStatus(final String status) {
			this.status = status;
			return this;
		}

		public ElectoralBoardBuilder setBoardMembers(final List<String> boardMembers) {
			this.boardMembers = boardMembers;
			return this;
		}

		public ElectoralBoardBuilder setElectionEvent(final Identifier electionEvent) {
			this.electionEvent = electionEvent;
			return this;
		}

		public ElectoralBoard build() {
			return new ElectoralBoard(id, defaultTitle, defaultDescription, alias, status, boardMembers, electionEvent);
		}
	}
}
