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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * The payload sent to the first mixing control component.
 */
@JsonPropertyOrder({ "encryptionGroup", "ciphertexts", "electionPublicKey", "signature", "signingPublicKey" })
@JsonDeserialize(as = MixnetInitialPayload.class, using = MixnetInitialPayload.MixnetInitialPayloadDeserializer.class)
public class MixnetInitialPayload implements MixnetPayload {

	@JsonProperty(required = true)
	private final GqGroup encryptionGroup;

	@JsonProperty(value = "ciphertexts", required = true)
	private final List<ElGamalMultiRecipientCiphertext> encryptedVotes;

	@JsonProperty(required = true)
	private final ElGamalMultiRecipientPublicKey electionPublicKey;

	@JsonProperty(required = true)
	private CryptoPrimitivesPayloadSignature signature;

	/**
	 * Constructs an unsigned payload.  All fields must be non null.
	 */
	public MixnetInitialPayload(final GqGroup encryptionGroup, final List<ElGamalMultiRecipientCiphertext> encryptedVotes,
			final ElGamalMultiRecipientPublicKey electionPublicKey) {

		checkNotNull(encryptionGroup);
		checkNotNull(encryptedVotes);
		checkNotNull(electionPublicKey);

		this.encryptionGroup = encryptionGroup;
		this.encryptedVotes = encryptedVotes;
		this.electionPublicKey = electionPublicKey;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	@Override
	public List<ElGamalMultiRecipientCiphertext> getEncryptedVotes() {
		return encryptedVotes;
	}

	@Override
	@JsonIgnore
	public ElGamalMultiRecipientPublicKey getRemainingElectionPublicKey() {
		return getElectionPublicKey();
	}

	public ElGamalMultiRecipientPublicKey getElectionPublicKey() {
		return electionPublicKey;
	}

	@Override
	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	@Override
	public MixnetInitialPayload setSignature(final CryptoPrimitivesPayloadSignature signature) {
		checkNotNull(signature);
		this.signature = signature;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final MixnetInitialPayload that = (MixnetInitialPayload) o;
		return Objects.equals(encryptionGroup, that.encryptionGroup) && Objects.equals(encryptedVotes, that.encryptedVotes) && Objects
				.equals(electionPublicKey, that.electionPublicKey) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, encryptedVotes, electionPublicKey, signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(this.encryptionGroup, HashableList.from(this.encryptedVotes), this.electionPublicKey);
	}

	/**
	 * Deserializes a json into a {@link MixnetInitialPayload}. This deserializer is needed when deserializing a payload outside of a {@link
	 * MixnetState}.
	 */
	static class MixnetInitialPayloadDeserializer extends JsonDeserializer<MixnetInitialPayload> {

		@Override
		public MixnetInitialPayload deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
			final ObjectMapper mapper = (ObjectMapper) parser.getCodec();

			final JsonNode node = mapper.readTree(parser);
			final JsonNode encryptionGroupNode = node.get("encryptionGroup");
			final GqGroup gqGroup = mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);
			final String groupAttribute = "group";

			final ElGamalMultiRecipientCiphertext[] encryptedVotesArray = mapper.reader().withAttribute(groupAttribute, gqGroup)
					.readValue(node.get("ciphertexts").toString(), ElGamalMultiRecipientCiphertext[].class);

			final ElGamalMultiRecipientPublicKey electionPublicKey = mapper.reader().withAttribute(groupAttribute, gqGroup)
					.readValue(node.get("electionPublicKey").toString(), ElGamalMultiRecipientPublicKey.class);

			final CryptoPrimitivesPayloadSignature signature = mapper.reader().readValue(node.get("signature").toString(), CryptoPrimitivesPayloadSignature.class);

			return new MixnetInitialPayload(gqGroup, Arrays.asList(encryptedVotesArray), electionPublicKey).setSignature(signature);
		}
	}

}
