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

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonPropertyOrder({ "tenantId", "electionEventId", "verificationCardSetId", "partialChoiceReturnCodesAllowList", "chunkId", "encryptionGroup",
		"setupComponentVerificationData", "combinedCorrectnessInformation", "signature" })
@JsonDeserialize(using = SetupComponentVerificationDataDeserializer.class)
public class SetupComponentVerificationDataPayload implements SignedPayload {

	@JsonProperty
	private final String tenantId;

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final String verificationCardSetId;

	@JsonProperty
	private final List<String> partialChoiceReturnCodesAllowList;

	@JsonProperty
	private final int chunkId;

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final List<SetupComponentVerificationData> setupComponentVerificationData;

	@JsonProperty
	private final CombinedCorrectnessInformation combinedCorrectnessInformation;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public SetupComponentVerificationDataPayload(
			@JsonProperty("tenantId")
			final String tenantId,
			@JsonProperty("electionEventId")
			final String electionEventId,
			@JsonProperty("verificationCardSetId")
			final String verificationCardSetId,
			@JsonProperty("partialChoiceReturnCodesAllowList")
			final List<String> partialChoiceReturnCodesAllowList,
			@JsonProperty("chunkId")
			final int chunkId,
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("setupComponentVerificationData")
			final List<SetupComponentVerificationData> setupComponentVerificationData,
			@JsonProperty("combinedCorrectnessInformation")
			final CombinedCorrectnessInformation combinedCorrectnessInformation,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {

		this.tenantId = checkNotNull(tenantId);
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.partialChoiceReturnCodesAllowList = checkNotNull(partialChoiceReturnCodesAllowList);
		this.chunkId = chunkId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.setupComponentVerificationData = checkNotNull(setupComponentVerificationData);
		this.combinedCorrectnessInformation = checkNotNull(combinedCorrectnessInformation);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Creates an unsigned payload.
	 */
	public SetupComponentVerificationDataPayload(final String tenantId, final String electionEventId, final String verificationCardSetId,
			final List<String> partialChoiceReturnCodesAllowList, final int chunkId, final GqGroup encryptionGroup,
			final List<SetupComponentVerificationData> setupComponentVerificationData, final CombinedCorrectnessInformation combinedCorrectnessInformation) {

		this.tenantId = checkNotNull(tenantId);
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.partialChoiceReturnCodesAllowList = checkNotNull(partialChoiceReturnCodesAllowList);
		this.chunkId = chunkId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.setupComponentVerificationData = checkNotNull(setupComponentVerificationData);
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

	public List<String> getPartialChoiceReturnCodesAllowList() {
		return partialChoiceReturnCodesAllowList;
	}

	public int getChunkId() {
		return chunkId;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public List<SetupComponentVerificationData> getSetupComponentVerificationData() {
		return setupComponentVerificationData;
	}

	public CombinedCorrectnessInformation getCombinedCorrectnessInformation() {
		return combinedCorrectnessInformation;
	}

	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	public void setSignature(CryptoPrimitivesPayloadSignature signature) {
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
		final SetupComponentVerificationDataPayload that = (SetupComponentVerificationDataPayload) o;
		return chunkId == that.chunkId
				&& tenantId.equals(that.tenantId)
				&& electionEventId.equals(that.electionEventId)
				&& verificationCardSetId.equals(that.verificationCardSetId)
				&& partialChoiceReturnCodesAllowList.equals(that.partialChoiceReturnCodesAllowList)
				&& encryptionGroup.equals(that.encryptionGroup)
				&& setupComponentVerificationData.equals(that.setupComponentVerificationData)
				&& combinedCorrectnessInformation.equals(that.combinedCorrectnessInformation)
				&& Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, electionEventId, verificationCardSetId, partialChoiceReturnCodesAllowList, chunkId, encryptionGroup,
				setupComponentVerificationData, combinedCorrectnessInformation, signature);
	}

	@Override
	public List<? extends Hashable> toHashableForm() {
		final List<HashableString> hashableAllowList = partialChoiceReturnCodesAllowList.stream()
				.map(HashableString::from)
				.toList();

		return List.of(HashableString.from(tenantId), HashableString.from(electionEventId), HashableString.from(verificationCardSetId),
				HashableList.from(hashableAllowList), HashableBigInteger.from(BigInteger.valueOf(chunkId)), encryptionGroup,
				HashableList.from(setupComponentVerificationData), combinedCorrectnessInformation);
	}
}
