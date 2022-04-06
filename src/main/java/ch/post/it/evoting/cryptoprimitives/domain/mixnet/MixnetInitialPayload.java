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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * The payload sent to the first mixing control component.
 */
@JsonPropertyOrder({ "electionEventId", "ballotBoxId", "encryptionGroup", "ciphertexts", "electionPublicKey", "signature" })
@JsonDeserialize(as = MixnetInitialPayload.class, using = MixnetInitialPayload.MixnetInitialPayloadDeserializer.class)
public class MixnetInitialPayload implements MixnetPayload {

	@JsonProperty(required = true)
	private final String electionEventId;

	@JsonProperty(required = true)
	private final String ballotBoxId;

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
	public MixnetInitialPayload(final String electionEventId, final String ballotBoxId, final GqGroup encryptionGroup,
			final List<ElGamalMultiRecipientCiphertext> encryptedVotes,
			final ElGamalMultiRecipientPublicKey electionPublicKey,
			final CryptoPrimitivesPayloadSignature signature) {

		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.encryptedVotes = checkNotNull(encryptedVotes);
		this.electionPublicKey = checkNotNull(electionPublicKey);
		this.signature = checkNotNull(signature);
	}

	public MixnetInitialPayload(final String electionEventId, final String ballotBoxId, final GqGroup encryptionGroup,
			final List<ElGamalMultiRecipientCiphertext> encryptedVotes,
			final ElGamalMultiRecipientPublicKey electionPublicKey) {

		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.encryptedVotes = checkNotNull(encryptedVotes);
		this.electionPublicKey = checkNotNull(electionPublicKey);
	}

	@Override
	public String getBallotBoxId() {
		return ballotBoxId;
	}

	@Override
	public String getElectionEventId() {
		return electionEventId;
	}

	@Override
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
	public void setSignature(final CryptoPrimitivesPayloadSignature signature) {
		this.signature = checkNotNull(signature);

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
		return Objects.equals(electionEventId, that.electionEventId) && Objects.equals(ballotBoxId, that.ballotBoxId)
				&& Objects.equals(encryptionGroup, that.encryptionGroup) && Objects.equals(encryptedVotes, that.encryptedVotes)
				&& Objects.equals(electionPublicKey, that.electionPublicKey) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, ballotBoxId, encryptionGroup, encryptedVotes, electionPublicKey, signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(this.electionEventId), HashableString.from(this.ballotBoxId), this.encryptionGroup,
				HashableList.from(this.encryptedVotes), this.electionPublicKey);
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

			final String electionEventId = mapper.readValue(node.get("electionEventId").toString(), String.class);
			final String ballotBoxId = mapper.readValue(node.get("ballotBoxId").toString(), String.class);

			final JsonNode encryptionGroupNode = node.get("encryptionGroup");
			final GqGroup gqGroup = mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);
			final String groupAttribute = "group";

			final ElGamalMultiRecipientCiphertext[] encryptedVotesArray = mapper.reader().withAttribute(groupAttribute, gqGroup)
					.readValue(node.get("ciphertexts").toString(), ElGamalMultiRecipientCiphertext[].class);

			final ElGamalMultiRecipientPublicKey electionPublicKey = mapper.reader().withAttribute(groupAttribute, gqGroup)
					.readValue(node.get("electionPublicKey").toString(), ElGamalMultiRecipientPublicKey.class);

			final CryptoPrimitivesPayloadSignature signature = mapper.reader()
					.readValue(node.get("signature").toString(), CryptoPrimitivesPayloadSignature.class);

			return new MixnetInitialPayload(electionEventId, ballotBoxId, gqGroup, Arrays.asList(encryptedVotesArray), electionPublicKey, signature);
		}
	}

}
