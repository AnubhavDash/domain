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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import static ch.post.it.evoting.cryptoprimitives.domain.ControlComponentConstants.NODE_IDS;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.hasNoDuplicates;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static ch.post.it.evoting.cryptoprimitives.utils.Validations.allEqual;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ch.post.it.evoting.cryptoprimitives.domain.ControlComponentConstants;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.domain.signature.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

@JsonPropertyOrder({ "electionEventId", "verificationCardSetId", "chunkId", "encryptionGroup", "controlComponentCodeShares", "nodeId",
		"signature" })
@JsonSerialize(using = ControlComponentCodeSharesPayloadSerializer.class)
@JsonDeserialize(using = ControlComponentCodeSharesPayloadDeserializer.class)
public class ControlComponentCodeSharesPayload implements SignedPayload {

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final String verificationCardSetId;

	@JsonProperty
	private final int chunkId;

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final List<ControlComponentCodeShare> controlComponentCodeShares;

	@JsonProperty
	private final int nodeId;

	@JsonProperty
	private CryptoPrimitivesSignature signature;

	@JsonCreator
	public ControlComponentCodeSharesPayload(

			@JsonProperty("electionEventId")
			final String electionEventId,

			@JsonProperty("verificationCardSetId")
			final String verificationCardSetId,

			@JsonProperty("chunkId")
			final int chunkId,

			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,

			@JsonProperty("controlComponentCodeShares")
			final List<ControlComponentCodeShare> controlComponentCodeShares,

			@JsonProperty("nodeId")
			final int nodeId,

			@JsonProperty("signature")
			final CryptoPrimitivesSignature signature) {

		this(electionEventId, verificationCardSetId, chunkId, encryptionGroup, controlComponentCodeShares, nodeId);
		this.signature = checkNotNull(signature);
	}

	public ControlComponentCodeSharesPayload(final String electionEventId, final String verificationCardSetId,
			final int chunkId, final GqGroup encryptionGroup, final List<ControlComponentCodeShare> controlComponentCodeShares, final int nodeId) {

		this.electionEventId = validateUUID(electionEventId);
		this.verificationCardSetId = validateUUID(verificationCardSetId);
		this.chunkId = chunkId;
		checkArgument(chunkId >= 0, "The chunk id must be non-negative.");
		checkArgument(NODE_IDS.contains(nodeId), "The node id must be part of the known node ids. [nodeId: %s]", nodeId);
		this.nodeId = nodeId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.controlComponentCodeShares = List.copyOf(checkNotNull(controlComponentCodeShares));

		checkArgument(!this.controlComponentCodeShares.isEmpty(), "The list of control component code shares must not be empty.");
		checkArgument(ControlComponentConstants.NODE_IDS.contains(nodeId),
				"The node id must be part of the known node ids. [nodeId: %s]", nodeId);

		checkArgument(allEqual(this.controlComponentCodeShares.stream()
						.map(ControlComponentCodeShare::exponentiatedEncryptedPartialChoiceReturnCodes)
						.map(ElGamalMultiRecipientCiphertext::getPhis), GroupVector::size),
				"All exponentiated encrypted Partial Choice Return Codes must have the same size.");

		checkArgument(this.controlComponentCodeShares.stream()
						.map(ControlComponentCodeShare::exponentiatedEncryptedConfirmationKey)
						.map(ElGamalMultiRecipientCiphertext::getGroup)
						.allMatch(group -> group.equals(encryptionGroup)),
				"The groups of the ControlComponentCodeShares must correspond to the encryption group.");

		checkArgument(hasNoDuplicates(this.controlComponentCodeShares.stream()
				.map(ControlComponentCodeShare::verificationCardId)
				.toList()), "The verification card IDs must all be distinct.");
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public int getChunkId() {
		return chunkId;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public List<ControlComponentCodeShare> getControlComponentCodeShares() {
		return controlComponentCodeShares;
	}

	public int getNodeId() {
		return nodeId;
	}

	public CryptoPrimitivesSignature getSignature() {
		return signature;
	}

	public void setSignature(final CryptoPrimitivesSignature signature) {
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
		final ControlComponentCodeSharesPayload that = (ControlComponentCodeSharesPayload) o;
		return chunkId == that.chunkId &&
				nodeId == that.nodeId &&
				electionEventId.equals(that.electionEventId) &&
				verificationCardSetId.equals(that.verificationCardSetId) &&
				encryptionGroup.equals(that.encryptionGroup) &&
				controlComponentCodeShares.equals(that.controlComponentCodeShares) &&
				Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, verificationCardSetId, chunkId, encryptionGroup, controlComponentCodeShares, nodeId,
				signature);
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(HashableString.from(electionEventId),
				HashableString.from(verificationCardSetId),
				HashableBigInteger.from(BigInteger.valueOf(chunkId)),
				encryptionGroup,
				HashableList.from(controlComponentCodeShares),
				HashableBigInteger.from(BigInteger.valueOf(nodeId)));
	}
}
