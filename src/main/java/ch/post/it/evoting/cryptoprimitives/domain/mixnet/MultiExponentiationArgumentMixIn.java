/*
 * Copyright 2021 Post CH Ltd
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import ch.post.it.evoting.cryptoprimitives.GroupVector;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationArgument;

@SuppressWarnings({ "java:S100", "java:S116", "java:S117", "unused" })
@JsonPropertyOrder({ "c_A_0", "c_B", "E", "a", "r", "b", "s", "tau" })
@JsonDeserialize(builder = MultiExponentiationArgument.Builder.class)
public abstract class MultiExponentiationArgumentMixIn {

	@JsonProperty
	GqElement c_A_0;

	@JsonProperty
	GroupVector<GqElement, GqGroup> c_B;

	@JsonProperty
	GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> E;

	@JsonProperty
	GroupVector<ZqElement, ZqGroup> a;

	@JsonProperty
	ZqElement r;

	@JsonProperty
	ZqElement b;

	@JsonProperty
	ZqElement s;

	@JsonProperty
	ZqElement tau;

	@JsonPOJOBuilder(withPrefix = "with_")
	public interface MultiExponentiationArgumentBuilderMixIn {

		@JsonDeserialize(using = GqGroupVectorDeserializer.class)
		MultiExponentiationArgument.Builder with_c_B(final GroupVector<GqElement, GqGroup> c_B);

		@JsonProperty("E")
		@JsonDeserialize(using = CiphertextGroupVectorDeserializer.class)
		MultiExponentiationArgument.Builder with_E(final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> E);

		@JsonDeserialize(using = ZqGroupVectorDeserializer.class)
		MultiExponentiationArgument.Builder with_a(final GroupVector<ZqElement, ZqGroup> a);

	}

}
