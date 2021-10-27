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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonPropertyOrder({ "tenantId", "electionEventId", "verificationCardSetId", "chunkId", "encryptionGroup", "returnCodeGenerationInputs",
		"combinedCorrectnessInformation", "signature" })
@JsonDeserialize(using = ReturnCodeGenerationRequestPayloadDeserializer.class)
public class ReturnCodeGenerationRequestPayload implements HashableList {

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
	private final List<ReturnCodeGenerationInput> returnCodeGenerationInputs;

	@JsonProperty
	private final CombinedCorrectnessInformation combinedCorrectnessInformation;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public ReturnCodeGenerationRequestPayload(
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
			@JsonProperty("returnCodeGenerationInputs")
			final List<ReturnCodeGenerationInput> returnCodeGenerationInputs,
			@JsonProperty("combinedCorrectnessInformation")
			final CombinedCorrectnessInformation combinedCorrectnessInformation,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {

		this.tenantId = checkNotNull(tenantId);
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.chunkId = chunkId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.returnCodeGenerationInputs = checkNotNull(returnCodeGenerationInputs);
		this.combinedCorrectnessInformation = checkNotNull(combinedCorrectnessInformation);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Creates an unsigned payload.
	 */
	public ReturnCodeGenerationRequestPayload(final String tenantId, final String electionEventId, final String verificationCardSetId,
			final int chunkId, final GqGroup encryptionGroup, final List<ReturnCodeGenerationInput> returnCodeGenerationInputs,
			final CombinedCorrectnessInformation combinedCorrectnessInformation) {

		this.tenantId = checkNotNull(tenantId);
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.chunkId = chunkId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.returnCodeGenerationInputs = checkNotNull(returnCodeGenerationInputs);
		this.combinedCorrectnessInformation = checkNotNull(combinedCorrectnessInformation);
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

	public List<ReturnCodeGenerationInput> getReturnCodeGenerationInputs() {
		return returnCodeGenerationInputs;
	}

	public CombinedCorrectnessInformation getCombinedCorrectnessInformation() {
		return combinedCorrectnessInformation;
	}

	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	public void setSignature(CryptoPrimitivesPayloadSignature signature) {
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
		final ReturnCodeGenerationRequestPayload that = (ReturnCodeGenerationRequestPayload) o;
		return chunkId == that.chunkId && tenantId.equals(that.tenantId) && electionEventId.equals(that.electionEventId) && verificationCardSetId
				.equals(that.verificationCardSetId) && encryptionGroup.equals(that.encryptionGroup) && returnCodeGenerationInputs
				.equals(that.returnCodeGenerationInputs) && combinedCorrectnessInformation.equals(that.combinedCorrectnessInformation) && Objects
				.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, electionEventId, verificationCardSetId, chunkId, encryptionGroup, returnCodeGenerationInputs,
				combinedCorrectnessInformation, signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(tenantId), HashableString.from(electionEventId), HashableString.from(verificationCardSetId),
				HashableBigInteger.from(BigInteger.valueOf(chunkId)), encryptionGroup, HashableList.from(returnCodeGenerationInputs),
				combinedCorrectnessInformation);
	}
}
