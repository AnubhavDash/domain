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

import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateActualVotingOption;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.post.it.evoting.cryptoprimitives.domain.validations.Validations;

/**
 * Contains a list with the election option attributes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElectionAttributes {

	private final String id;
	private final List<String> related;
	private final boolean correctness;
	private final String alias;

	@JsonCreator
	public ElectionAttributes(
			@JsonProperty("id")
			final String id,
			@JsonProperty("alias")
			final String alias,
			@JsonProperty("related")
			final List<String> related,
			@JsonProperty("correctness")
			final boolean correctness) {
		this.id = validateUUID(id);
		this.alias = checkNotNull(alias);
		this.related = checkNotNull(related);
		this.correctness = correctness;

		// the attribute's alias represents the actual voting option.
		validateActualVotingOption(alias);
		related.forEach(Validations::validateUUID);
	}

	@JsonIgnore
	public boolean isCorrectness() {
		return correctness;
	}

	@JsonGetter("correctness")
	public String getCorrectness() {
		return String.valueOf(correctness);
	}

	public String getId() {
		return id;
	}

	public String getAlias() {
		return alias;
	}

	public List<String> getRelated() {
		return related;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ElectionAttributes that = (ElectionAttributes) o;
		return correctness == that.correctness && Objects.equals(id, that.id) && Objects.equals(related, that.related)
				&& Objects.equals(alias, that.alias);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, related, correctness, alias);
	}
}
