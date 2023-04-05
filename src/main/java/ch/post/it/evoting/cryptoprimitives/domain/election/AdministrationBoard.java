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

import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.MAXIMUM_NUMBER_OF_BOARD_MEMBERS;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.MINIMUM_NUMBER_OF_BOARD_MEMBERS;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateAlias;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateBoardMembers;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultDescription;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultTitle;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateStatus;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates the information contained within an administration authority.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdministrationBoard(String id,
								  String defaultTitle,
								  String defaultDescription,
								  String alias,
								  Integer minimumThreshold,
								  String status,
								  List<String> boardMembers) {

	public AdministrationBoard {
		validateUUID(id);
		validateDefaultTitle(defaultTitle);
		validateDefaultDescription(defaultDescription);
		validateAlias(alias);
		validateStatus(status);
		validateBoardMembers(boardMembers);

		checkArgument(MINIMUM_NUMBER_OF_BOARD_MEMBERS <= minimumThreshold && minimumThreshold <= MAXIMUM_NUMBER_OF_BOARD_MEMBERS,
				"The minimum threshold must be in the range [%s, %s]. [minimumThreshold: %s, administrationBoardId: %s]",
				MINIMUM_NUMBER_OF_BOARD_MEMBERS, MAXIMUM_NUMBER_OF_BOARD_MEMBERS, minimumThreshold, id);
		checkArgument(minimumThreshold <= boardMembers.size(),
				"The minimum threshold must be at most equal to the number of board members. [administrationBoardId: %s]", id);
	}

	@JsonGetter("minimumThreshold")
	public String getMinimumThreshold() {
		return String.valueOf(minimumThreshold);
	}

	public static final class AdministrationBoardBuilder {
		private String id;
		private String defaultTitle;
		private String defaultDescription;
		private String alias;
		private Integer minimumThreshold;
		private String status;
		private List<String> boardMembers;

		public AdministrationBoardBuilder setId(final String id) {
			this.id = id;
			return this;
		}

		public AdministrationBoardBuilder setDefaultTitle(final String defaultTitle) {
			this.defaultTitle = defaultTitle;
			return this;
		}

		public AdministrationBoardBuilder setDefaultDescription(final String defaultDescription) {
			this.defaultDescription = defaultDescription;
			return this;
		}

		public AdministrationBoardBuilder setAlias(final String alias) {
			this.alias = alias;
			return this;
		}

		public AdministrationBoardBuilder setMinimumThreshold(final Integer minimumThreshold) {
			this.minimumThreshold = minimumThreshold;
			return this;
		}

		public AdministrationBoardBuilder setStatus(final String status) {
			this.status = status;
			return this;
		}

		public AdministrationBoardBuilder setBoardMembers(final List<String> boardMembers) {
			this.boardMembers = boardMembers;
			return this;
		}

		public AdministrationBoard build() {
			return new AdministrationBoard(id, defaultTitle, defaultDescription, alias, minimumThreshold, status, boardMembers);
		}
	}
}
