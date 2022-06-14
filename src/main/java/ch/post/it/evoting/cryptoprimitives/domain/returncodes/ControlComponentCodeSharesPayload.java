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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonPropertyOrder({ "tenantId", "electionEventId", "verificationCardSetId", "chunkId", "encryptionGroup", "controlComponentCodeShares", "nodeId",
		"signature" })
@JsonDeserialize(using = ControlComponentCodeSharesPayloadDeserializer.class)
public class ControlComponentCodeSharesPayload implements SignedPayload {

	@JsonProperty
	private final String tenantId;

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
			@JsonProperty("tenantId")
			final String tenantId,
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

		this.tenantId = checkNotNull(tenantId);
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.chunkId = chunkId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.controlComponentCodeShares = checkNotNull(controlComponentCodeShares);
		this.nodeId = nodeId;
		this.signature = checkNotNull(signature);
	}

	public ControlComponentCodeSharesPayload(final String tenantId, final String electionEventId, final String verificationCardSetId,
			final int chunkId, final GqGroup encryptionGroup, final List<ControlComponentCodeShare> controlComponentCodeShares, final int nodeId) {

		this.tenantId = checkNotNull(tenantId);
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.chunkId = chunkId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.controlComponentCodeShares = checkNotNull(controlComponentCodeShares);
		this.nodeId = nodeId;
	}

	public String getTenantId() {
		return tenantId;
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

	public void setSignature(CryptoPrimitivesSignature signature) {
		this.signature = signature;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ControlComponentCodeSharesPayload that = (ControlComponentCodeSharesPayload) o;
		return chunkId == that.chunkId && nodeId == that.nodeId && tenantId.equals(that.tenantId) && electionEventId.equals(that.electionEventId)
				&& verificationCardSetId.equals(that.verificationCardSetId) && encryptionGroup.equals(that.encryptionGroup)
				&& controlComponentCodeShares.equals(that.controlComponentCodeShares) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects
				.hash(tenantId, electionEventId, verificationCardSetId, chunkId, encryptionGroup, controlComponentCodeShares, nodeId, signature);
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(HashableString.from(tenantId), HashableString.from(electionEventId), HashableString.from(verificationCardSetId),
				HashableBigInteger.from(BigInteger.valueOf(chunkId)), encryptionGroup, HashableList.from(controlComponentCodeShares),
				HashableBigInteger.from(BigInteger.valueOf(nodeId)));
	}
}
