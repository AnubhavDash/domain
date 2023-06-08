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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateNonBlankUCS;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateXsToken;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates the information related to an election option.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElectionOption {

	private final String id;
	private final String attribute;
	private final String semantics;
	private String representation;

	@JsonCreator
	public ElectionOption(
			@JsonProperty("id")
			final String id,
			@JsonProperty("attribute")
			final String attribute,
			@JsonProperty("semantics")
			final String semantics,
			@JsonProperty("representation")
			final String representation) {
		this.id = validateUUID(id);
		this.attribute = validateUUID(attribute);
		this.semantics = validateNonBlankUCS(semantics);
		this.representation = validateXsToken(representation);
	}

	public String getId() {
		return this.id;
	}

	public String getAttribute() {
		return this.attribute;
	}

	public String getSemantics() {
		return this.semantics;
	}

	public String getRepresentation() {
		return this.representation;
	}

	public void setRepresentation(final String representation) {
		this.representation = representation;
	}

	public boolean hasRepresentation() {
		return (representation != null) && (representation.length() > 0);
	}

}
