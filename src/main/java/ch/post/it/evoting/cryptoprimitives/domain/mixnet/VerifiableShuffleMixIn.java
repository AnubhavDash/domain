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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "shuffledCiphertexts", "shuffleArgument" })
public abstract class VerifiableShuffleMixIn {

	@JsonProperty
	@JsonDeserialize(using = CiphertextGroupVectorDeserializer.class)
	GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts;

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	ShuffleArgument shuffleArgument;

	@JsonCreator
	VerifiableShuffleMixIn(
			@JsonProperty(value = "shuffledCiphertexts", required = true)
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> shuffledCiphertexts,
			@JsonProperty(value = "shuffleArgument", required = true)
			final ShuffleArgument shuffleArgument) {
	}

}
