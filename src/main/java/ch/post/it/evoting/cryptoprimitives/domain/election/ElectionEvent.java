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
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates the information contained within an election event.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ElectionEvent(String id,
							String defaultTitle,
							String defaultDescription,
							String alias,
							LocalDateTime dateFrom,
							LocalDateTime dateTo,
							Integer gracePeriod,
							Identifier administrationBoard) {

	public ElectionEvent {
		validateUUID(id);
		validateDefaultTitle(defaultTitle);
		validateDefaultDescription(defaultDescription);
		validateAlias(alias);
		validateDateFromDateTo(dateFrom, dateTo);
		validateGracePeriod(gracePeriod);
		checkNotNull(administrationBoard);
	}

	@JsonGetter("gracePeriod")
	public String getGracePeriod() {
		return String.valueOf(gracePeriod);
	}

	public static final class ElectionEventBuilder {
		private String id;
		private String defaultTitle;
		private String defaultDescription;
		private String alias;
		private LocalDateTime dateFrom;
		private LocalDateTime dateTo;
		private Integer gracePeriod;
		private Identifier administrationBoard;

		public ElectionEventBuilder setId(final String id) {
			this.id = id;
			return this;
		}

		public ElectionEventBuilder setDefaultTitle(final String defaultTitle) {
			this.defaultTitle = defaultTitle;
			return this;
		}

		public ElectionEventBuilder setDefaultDescription(final String defaultDescription) {
			this.defaultDescription = defaultDescription;
			return this;
		}

		public ElectionEventBuilder setAlias(final String alias) {
			this.alias = alias;
			return this;
		}

		public ElectionEventBuilder setDateFrom(final LocalDateTime dateFrom) {
			this.dateFrom = dateFrom;
			return this;
		}

		public ElectionEventBuilder setDateTo(final LocalDateTime dateTo) {
			this.dateTo = dateTo;
			return this;
		}

		public ElectionEventBuilder setGracePeriod(final Integer gracePeriod) {
			this.gracePeriod = gracePeriod;
			return this;
		}

		public ElectionEventBuilder setAdministrationBoard(final Identifier administrationBoard) {
			this.administrationBoard = administrationBoard;
			return this;
		}

		public ElectionEvent build() {
			return new ElectionEvent(id, defaultTitle, defaultDescription, alias, dateFrom, dateTo, gracePeriod, administrationBoard);
		}
	}
}
