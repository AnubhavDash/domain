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

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.domain.signature.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.BaseEncodingFactory;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

@JsonPropertyOrder({ "electionEventId", "verificationCardSetId", "partialChoiceReturnCodesAllowList", "chunkId", "encryptionGroup",
		"setupComponentVerificationData", "combinedCorrectnessInformation", "signature" })
@JsonSerialize(using = SetupComponentVerificationDataPayloadSerializer.class)
@JsonDeserialize(using = SetupComponentVerificationDataPayloadDeserializer.class)
public class SetupComponentVerificationDataPayload implements SignedPayload {

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
	private CryptoPrimitivesSignature signature;

	@JsonCreator
	public SetupComponentVerificationDataPayload(

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
			final CryptoPrimitivesSignature signature) {

		this(electionEventId, verificationCardSetId, partialChoiceReturnCodesAllowList, chunkId, encryptionGroup,
				setupComponentVerificationData, combinedCorrectnessInformation);
		this.signature = checkNotNull(signature);
	}

	public SetupComponentVerificationDataPayload(final String electionEventId, final String verificationCardSetId,
			final List<String> partialChoiceReturnCodesAllowList, final int chunkId, final GqGroup encryptionGroup,
			final List<SetupComponentVerificationData> setupComponentVerificationData,
			final CombinedCorrectnessInformation combinedCorrectnessInformation) {

		this.electionEventId = validateUUID(electionEventId);
		this.verificationCardSetId = validateUUID(verificationCardSetId);
		this.partialChoiceReturnCodesAllowList = List.copyOf(checkNotNull(partialChoiceReturnCodesAllowList));
		this.chunkId = chunkId;
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.setupComponentVerificationData = checkNotNull(setupComponentVerificationData);
		this.combinedCorrectnessInformation = checkNotNull(combinedCorrectnessInformation);

		checkNotNull(this.partialChoiceReturnCodesAllowList);

		checkArgument(!this.partialChoiceReturnCodesAllowList.isEmpty(), "The partial Choice Return Codes Allow List must not be empty.");
		checkArgument(this.partialChoiceReturnCodesAllowList.stream().allMatch(Objects::nonNull),
				"The partial Choice Return Codes Allow List must not contain null elements.");
		checkArgument(this.partialChoiceReturnCodesAllowList.stream().noneMatch(String::isBlank),
				"The partial Choice Return Codes Allow List must not contain empty or whitespace strings.");
		// The partial Choice Return Codes Allow List must only contain Base64 strings.
		this.partialChoiceReturnCodesAllowList.forEach(BaseEncodingFactory.createBase64()::base64Decode);

		checkArgument(!this.setupComponentVerificationData.isEmpty(), "The setup component verification data must not be empty.");
		checkArgument(this.setupComponentVerificationData.stream().allMatch(Objects::nonNull),
				"The setup component verification data list must not contain null elements.");

		checkArgument(allEqual(this.setupComponentVerificationData.stream().map(SetupComponentVerificationData::verificationCardPublicKey),
				ElGamalMultiRecipientPublicKey::getGroup), "All setup verification data must have the same group.");
		checkArgument(this.encryptionGroup.equals(this.setupComponentVerificationData.get(0).verificationCardPublicKey().getGroup()),
				"The groups of the setup component verification data must be equal to the encryption group.");

		checkArgument(allEqual(this.setupComponentVerificationData.stream()
						.map(SetupComponentVerificationData::encryptedHashedSquaredPartialChoiceReturnCodes)
						.map(ElGamalMultiRecipientCiphertext::getPhis), GroupVector::size),
				"All encrypted hashed squared Partial Choice Return Codes must have the same size.");

		checkArgument(hasNoDuplicates(this.setupComponentVerificationData.stream()
				.map(SetupComponentVerificationData::verificationCardId)
				.toList()), "The verification card IDs must all be distinct.");
		checkArgument(this.partialChoiceReturnCodesAllowList.size() ==
						this.setupComponentVerificationData.size() * this.combinedCorrectnessInformation.getTotalNumberOfVotingOptions(),
				"The number of elements in the partial Choice Return Codes Allow List must correspond to the size of the SetupComponentVerificationData multiplied with the total number of voting options.");
		checkArgument(this.setupComponentVerificationData.stream()
						.map(SetupComponentVerificationData::encryptedHashedSquaredPartialChoiceReturnCodes)
						.map(ElGamalMultiRecipientCiphertext::size)
						.allMatch(size -> Objects.equals(size, this.combinedCorrectnessInformation.getTotalNumberOfVotingOptions())),
				"The size of the encrypted hashed squared Partial Choice Return Codes must correspond to the total number of voting options.");
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
		return List.copyOf(setupComponentVerificationData);
	}

	public CombinedCorrectnessInformation getCombinedCorrectnessInformation() {
		return combinedCorrectnessInformation;
	}

	public CryptoPrimitivesSignature getSignature() {
		return signature;
	}

	public void setSignature(CryptoPrimitivesSignature signature) {
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
		return chunkId == that.chunkId &&
				electionEventId.equals(that.electionEventId) &&
				verificationCardSetId.equals(that.verificationCardSetId) &&
				partialChoiceReturnCodesAllowList.equals(that.partialChoiceReturnCodesAllowList) &&
				encryptionGroup.equals(that.encryptionGroup) &&
				setupComponentVerificationData.equals(that.setupComponentVerificationData) &&
				combinedCorrectnessInformation.equals(that.combinedCorrectnessInformation) &&
				Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, verificationCardSetId, partialChoiceReturnCodesAllowList, chunkId, encryptionGroup,
				setupComponentVerificationData, combinedCorrectnessInformation, signature);
	}

	@Override
	public List<Hashable> toHashableForm() {
		final List<HashableString> hashableAllowList = partialChoiceReturnCodesAllowList.stream()
				.map(HashableString::from)
				.toList();

		return List.of(
				HashableString.from(electionEventId),
				HashableString.from(verificationCardSetId),
				HashableList.from(hashableAllowList),
				HashableBigInteger.from(BigInteger.valueOf(chunkId)),
				encryptionGroup,
				HashableList.from(setupComponentVerificationData),
				combinedCorrectnessInformation);
	}
}
