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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "e", "z" })
public abstract class DecryptionProofMixIn {

	@JsonProperty
	ZqElement e;

	@JsonProperty
	@JsonDeserialize(using = ZqGroupVectorDeserializer.class)
	abstract GroupVector<ZqElement, ZqGroup> z();

	@JsonCreator
	DecryptionProofMixIn(
			@JsonProperty(value = "e", required = true)
			final ZqElement e,
			@JsonProperty(value = "z", required = true)
			final GroupVector<ZqElement, ZqGroup> z) {
	}

	@JsonIgnore
	abstract ZqGroup getGroup();

}
