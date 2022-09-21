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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains a list with the election option attributes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElectionAttributes {

	private final String id;
	private final List<String> related;
	private final boolean correctness;
	private String alias;

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
		this.id = id;
		this.alias = alias;
		this.related = related;
		this.correctness = correctness;
	}

	/**
	 * @param alias the alias to set. Must be non-null and non-blank.
	 * @throws NullPointerException     if the alias is null.
	 * @throws IllegalArgumentException if the alias is blank.
	 */
	public void setAlias(final String alias) {
		checkNotNull(alias);
		checkArgument(!alias.isBlank(), "The alias to be set cannot be blank.");

		this.alias = alias;
	}

	@JsonGetter("correctness")
	public boolean isCorrectness() {
		return correctness;
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
}
