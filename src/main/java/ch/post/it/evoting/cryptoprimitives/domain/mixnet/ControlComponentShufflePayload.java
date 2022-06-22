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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

/**
 * Encapsulates the output of a mixing / decryption operation.
 */
@JsonPropertyOrder({ "encryptionGroup", "electionEventId", "ballotBoxId", "nodeId", "verifiableDecryptions", "verifiableShuffle", "signature" })
@JsonDeserialize(using = ControlComponentShufflePayloadDeserializer.class)
public class ControlComponentShufflePayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final String ballotBoxId;

	@JsonProperty
	private final int nodeId;

	@JsonProperty
	private final VerifiableDecryptions verifiableDecryptions;

	@JsonProperty
	private final VerifiableShuffle verifiableShuffle;

	@JsonProperty
	private CryptoPrimitivesSignature signature;

	@JsonCreator
	public ControlComponentShufflePayload(
			@JsonProperty(value = "encryptionGroup", required = true)
			final GqGroup encryptionGroup,
			@JsonProperty(value = "electionEventId", required = true)
			final String electionEventId,
			@JsonProperty(value = "ballotBoxId", required = true)
			final String ballotBoxId,
			@JsonProperty(value = "nodeId", required = true)
			final int nodeId,
			@JsonProperty(value = "verifiableDecryptions", required = true)
			final VerifiableDecryptions verifiableDecryptions,
			@JsonProperty(value = "verifiableShuffle", required = true)
			final VerifiableShuffle verifiableShuffle,
			@JsonProperty(value = "signature", required = true)
			final CryptoPrimitivesSignature signature) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.nodeId = nodeId;
		this.verifiableDecryptions = checkNotNull(verifiableDecryptions);
		this.verifiableShuffle = checkNotNull(verifiableShuffle);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Constructs an unsigned payload.
	 */
	public ControlComponentShufflePayload(final GqGroup encryptionGroup, final String electionEventId, final String ballotBoxId, final int nodeId,
			final VerifiableDecryptions verifiableDecryptions, final VerifiableShuffle verifiableShuffle) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.nodeId = nodeId;
		this.verifiableDecryptions = checkNotNull(verifiableDecryptions);
		this.verifiableShuffle = checkNotNull(verifiableShuffle);
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public int getNodeId() {
		return nodeId;
	}

	public VerifiableDecryptions getVerifiableDecryptions() {
		return verifiableDecryptions;
	}

	public VerifiableShuffle getVerifiableShuffle() {
		return verifiableShuffle;
	}

	@Override
	public CryptoPrimitivesSignature getSignature() {
		return signature;
	}

	@Override
	public void setSignature(final CryptoPrimitivesSignature signature) {
		this.signature = checkNotNull(signature);
	}

	@Override
	public List<? extends Hashable> toHashableForm() {
		return List.of(this.encryptionGroup, HashableString.from(this.electionEventId), HashableString.from(this.ballotBoxId),
				HashableBigInteger.from(BigInteger.valueOf(this.nodeId)), this.verifiableDecryptions, this.verifiableShuffle);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ControlComponentShufflePayload that = (ControlComponentShufflePayload) o;
		return nodeId == that.nodeId && encryptionGroup.equals(that.encryptionGroup) && electionEventId.equals(that.electionEventId)
				&& ballotBoxId.equals(that.ballotBoxId) && verifiableDecryptions.equals(that.verifiableDecryptions) && verifiableShuffle.equals(
				that.verifiableShuffle) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, electionEventId, ballotBoxId, nodeId, verifiableDecryptions, verifiableShuffle, signature);
	}

}
