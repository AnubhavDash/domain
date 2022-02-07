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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

/**
 * Encapsulates the output of a mixing / decryption operation. If the ballot box contained only one vote, the verifiableShuffle is null, since
 * shuffling only works with at least two votes.
 */
@JsonPropertyOrder({ "electionEventId", "ballotBoxId", "encryptionGroup", "verifiableDecryptions", "verifiableShuffle", "remainingElectionPublicKey",
		"previousRemainingElectionPublicKey", "nodeElectionPublicKey", "nodeId", "signature", "signingPublicKey" })
@JsonDeserialize(as = MixnetShufflePayload.class, using = MixnetShufflePayloadDeserializer.class)
public class MixnetShufflePayload implements MixnetPayload {

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final String ballotBoxId;

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final VerifiableDecryptions verifiableDecryptions;

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final VerifiableShuffle verifiableShuffle;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey remainingElectionPublicKey;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey nodeElectionPublicKey;

	@JsonProperty
	private final int nodeId;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public 	MixnetShufflePayload(
			@JsonProperty(value = "electionEventId", required = true)
			final String electionEventId,
			@JsonProperty(value = "ballotBoxId", required = true)
			final String ballotBoxId,
			@JsonProperty(value = "encryptionGroup", required = true)
			final GqGroup encryptionGroup,
			@JsonProperty(value = "verifiableDecryptions", required = true)
			final VerifiableDecryptions verifiableDecryptions,
			@JsonProperty("verifiableShuffle")
			final VerifiableShuffle verifiableShuffle,
			@JsonProperty(value = "remainingElectionPublicKey", required = true)
			final ElGamalMultiRecipientPublicKey remainingElectionPublicKey,
			@JsonProperty(value = "previousRemainingElectionPublicKey", required = true)
			final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey,
			@JsonProperty(value = "nodeElectionPublicKey", required = true)
			final ElGamalMultiRecipientPublicKey nodeElectionPublicKey,
			@JsonProperty(value = "nodeId", required = true)
			final int nodeId,
			@JsonProperty(value = "signature", required = true)
			final CryptoPrimitivesPayloadSignature signature) {

		this.electionEventId = electionEventId;
		this.ballotBoxId = ballotBoxId;
		this.encryptionGroup = encryptionGroup;
		this.verifiableDecryptions = verifiableDecryptions;
		this.verifiableShuffle = verifiableShuffle;
		this.remainingElectionPublicKey = remainingElectionPublicKey;
		this.previousRemainingElectionPublicKey = previousRemainingElectionPublicKey;
		this.nodeElectionPublicKey = nodeElectionPublicKey;
		this.nodeId = nodeId;
		this.signature = signature;
	}

	/**
	 * Constructs an unsigned payload.
	 */
	public MixnetShufflePayload(final String electionEventId, final String ballotBoxId, final GqGroup encryptionGroup,
			final VerifiableDecryptions verifiableDecryptions,
			final VerifiableShuffle verifiableShuffle, final ElGamalMultiRecipientPublicKey remainingElectionPublicKey,
			final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey, final ElGamalMultiRecipientPublicKey nodeElectionPublicKey,
			final int nodeId) {

		checkNotNull(electionEventId);
		checkNotNull(ballotBoxId);
		checkNotNull(encryptionGroup);
		checkNotNull(verifiableDecryptions);
		checkNotNull(remainingElectionPublicKey);
		checkNotNull(previousRemainingElectionPublicKey);
		checkNotNull(nodeElectionPublicKey);

		validateUUID(electionEventId);
		validateUUID(ballotBoxId);

		this.electionEventId = electionEventId;
		this.ballotBoxId = ballotBoxId;
		this.encryptionGroup = encryptionGroup;
		this.verifiableDecryptions = verifiableDecryptions;
		this.verifiableShuffle = verifiableShuffle;
		this.remainingElectionPublicKey = remainingElectionPublicKey;
		this.previousRemainingElectionPublicKey = previousRemainingElectionPublicKey;
		this.nodeElectionPublicKey = nodeElectionPublicKey;
		this.nodeId = nodeId;
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

	public VerifiableDecryptions getVerifiableDecryptions() {
		return verifiableDecryptions;
	}

	public VerifiableShuffle getVerifiableShuffle() {
		return verifiableShuffle;
	}

	@Override
	@JsonIgnore
	public List<ElGamalMultiRecipientCiphertext> getEncryptedVotes() {
		return verifiableDecryptions.getCiphertexts();
	}

	@Override
	public ElGamalMultiRecipientPublicKey getRemainingElectionPublicKey() {
		return remainingElectionPublicKey;
	}

	public ElGamalMultiRecipientPublicKey getPreviousRemainingElectionPublicKey() {
		return previousRemainingElectionPublicKey;
	}

	public ElGamalMultiRecipientPublicKey getNodeElectionPublicKey() {
		return nodeElectionPublicKey;
	}

	public int getNodeId() {
		return nodeId;
	}

	@Override
	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	@Override
	public MixnetShufflePayload setSignature(final CryptoPrimitivesPayloadSignature signature) {
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
		final MixnetShufflePayload that = (MixnetShufflePayload) o;
		return nodeId == that.nodeId && Objects.equals(electionEventId, that.electionEventId) && Objects
				.equals(ballotBoxId, that.ballotBoxId) && Objects.equals(encryptionGroup, that.encryptionGroup) && Objects
				.equals(verifiableDecryptions, that.verifiableDecryptions) && Objects.equals(verifiableShuffle, that.verifiableShuffle)
				&& Objects.equals(remainingElectionPublicKey, that.remainingElectionPublicKey) && Objects
				.equals(previousRemainingElectionPublicKey, that.previousRemainingElectionPublicKey) && Objects
				.equals(nodeElectionPublicKey, that.nodeElectionPublicKey) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, ballotBoxId, encryptionGroup, verifiableDecryptions, verifiableShuffle, remainingElectionPublicKey,
				previousRemainingElectionPublicKey, nodeElectionPublicKey, nodeId, signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		final int numberOfVotes = this.getEncryptedVotes().size();
		if (numberOfVotes > 1) {
			return ImmutableList.of(HashableString.from(this.electionEventId), HashableString.from(this.ballotBoxId), this.encryptionGroup,
					this.verifiableDecryptions, this.verifiableShuffle, this.remainingElectionPublicKey, this.previousRemainingElectionPublicKey,
					this.nodeElectionPublicKey, HashableBigInteger.from(BigInteger.valueOf(this.nodeId)));
		} else {
			return ImmutableList.of(HashableString.from(this.electionEventId), HashableString.from(this.ballotBoxId), this.encryptionGroup,
					this.verifiableDecryptions, this.remainingElectionPublicKey, this.previousRemainingElectionPublicKey, this.nodeElectionPublicKey,
					HashableBigInteger.from(BigInteger.valueOf(this.nodeId)));
		}
	}
}
