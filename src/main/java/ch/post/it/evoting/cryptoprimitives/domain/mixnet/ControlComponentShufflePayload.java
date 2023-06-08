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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.ControlComponentConstants;
import ch.post.it.evoting.cryptoprimitives.domain.signature.SignedPayload;
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

			@JsonProperty(value = "encryptionGroup")
			final GqGroup encryptionGroup,

			@JsonProperty(value = "electionEventId")
			final String electionEventId,

			@JsonProperty(value = "ballotBoxId")
			final String ballotBoxId,

			@JsonProperty(value = "nodeId")
			final int nodeId,

			@JsonProperty(value = "verifiableDecryptions")
			final VerifiableDecryptions verifiableDecryptions,

			@JsonProperty(value = "verifiableShuffle")
			final VerifiableShuffle verifiableShuffle,

			@JsonProperty(value = "signature")
			final CryptoPrimitivesSignature signature) {

		this(encryptionGroup, electionEventId, ballotBoxId, nodeId, verifiableDecryptions, verifiableShuffle);
		this.signature = checkNotNull(signature);
	}

	public ControlComponentShufflePayload(final GqGroup encryptionGroup, final String electionEventId, final String ballotBoxId, final int nodeId,
			final VerifiableDecryptions verifiableDecryptions, final VerifiableShuffle verifiableShuffle) {
		this.encryptionGroup = encryptionGroup;
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.nodeId = nodeId;
		this.verifiableDecryptions = checkNotNull(verifiableDecryptions);
		this.verifiableShuffle = checkNotNull(verifiableShuffle);

		checkArgument(ControlComponentConstants.NODE_IDS.contains(nodeId),
				"The node id must be part of the known node ids. [nodeId: %s]", nodeId);
		checkArgument(encryptionGroup.equals(verifiableDecryptions.getGroup()),
				"The verifiable decryptions' group should be equal to the encryption group.");
		checkArgument(encryptionGroup.equals(verifiableShuffle.shuffleArgument().getGroup()),
				"The verifiable shuffle's group should be equal to the encryption group.");
		checkArgument(verifiableDecryptions.get_N() == verifiableShuffle.shuffledCiphertexts().size(),
				"The verifiable decryptions and the verifiable shuffle must have the same number of ciphertexts.");
		checkArgument(verifiableDecryptions.get_l() == verifiableShuffle.shuffledCiphertexts().getElementSize(),
				"The verifiable decryptions' and the verifiable shuffle's ciphertexts must have the same element size.");
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
	public List<Hashable> toHashableForm() {
		return List.of(encryptionGroup,
				HashableString.from(electionEventId),
				HashableString.from(ballotBoxId),
				HashableBigInteger.from(BigInteger.valueOf(nodeId)),
				verifiableDecryptions,
				verifiableShuffle);
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
		return nodeId == that.nodeId &&
				encryptionGroup.equals(that.encryptionGroup) &&
				electionEventId.equals(that.electionEventId) &&
				ballotBoxId.equals(that.ballotBoxId) &&
				verifiableDecryptions.equals(that.verifiableDecryptions) &&
				verifiableShuffle.equals(that.verifiableShuffle) &&
				Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, electionEventId, ballotBoxId, nodeId, verifiableDecryptions, verifiableShuffle, signature);
	}
}
