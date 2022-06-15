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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;

@SuppressWarnings("java:S100")
@JsonPropertyOrder({ "ciphertexts", "decryptionProofs" })
public abstract class VerifiableDecryptionsMixIn {

	@JsonProperty
	@JsonDeserialize(using = CiphertextGroupVectorDeserializer.class)
	GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts;

	@JsonProperty
	@JsonDeserialize(using = DecryptionProofGroupVectorDeserializer.class)
	GroupVector<DecryptionProof, ZqGroup> decryptionProofs;

	@JsonIgnore
	abstract GqGroup getGroup();

	@JsonIgnore
	abstract int get_N();

	@JsonIgnore
	abstract int get_l();

	@JsonCreator
	VerifiableDecryptionsMixIn(
			@JsonProperty(value = "ciphertexts", required = true)
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts,
			@JsonProperty(value = "decryptionProofs", required = true)
			final GroupVector<DecryptionProof, ZqGroup> decryptionProofs) {
	}

}
