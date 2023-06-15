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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "gamma", "phis" })
public abstract class ElGamalMultiRecipientCiphertextMixIn {

	@JsonProperty
	GqElement gamma;

	@JsonProperty
	GroupVector<GqElement, GqGroup> phis;

	@JsonIgnore
	GqGroup group;

	@JsonCreator
	static ElGamalMultiRecipientCiphertext create(
			@JsonProperty(value = "gamma", required = true)
			final GqElement gamma,
			@JsonProperty(value = "phis", required = true)
			final List<GqElement> phis) {
		return null;
	}

	@JsonIgnore
	abstract GroupVector<GqElement, GqGroup> getPhis();

}
