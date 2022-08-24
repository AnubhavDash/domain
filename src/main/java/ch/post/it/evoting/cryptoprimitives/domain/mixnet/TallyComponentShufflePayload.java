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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;

/**
 * Value class representing the final result of a mixnet.
 */
@JsonPropertyOrder({ "encryptionGroup", "electionEventId", "ballotBoxId", "verifiableShuffle", "verifiablePlaintextDecryption", "signature" })
@JsonDeserialize(using = TallyComponentShufflePayloadDeserializer.class)
public class TallyComponentShufflePayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final String ballotBoxId;

	@JsonProperty
	private final VerifiableShuffle verifiableShuffle;

	@JsonProperty
	private final VerifiablePlaintextDecryption verifiablePlaintextDecryption;

	@JsonProperty
	private CryptoPrimitivesSignature signature;

	@JsonCreator
	public TallyComponentShufflePayload(
			@JsonProperty(value = "encryptionGroup")
			final GqGroup encryptionGroup,

			@JsonProperty(value = "electionEventId")
			final String electionEventId,

			@JsonProperty(value = "ballotBoxId")
			final String ballotBoxId,

			@JsonProperty(value = "verifiableShuffle")
			final VerifiableShuffle verifiableShuffle,

			@JsonProperty(value = "verifiablePlaintextDecryption")
			final VerifiablePlaintextDecryption verifiablePlaintextDecryption,

			@JsonProperty(value = "signature")
			final CryptoPrimitivesSignature signature) {

		this(encryptionGroup, electionEventId, ballotBoxId, verifiableShuffle, verifiablePlaintextDecryption);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Constructs an unsigned payload.
	 */
	public TallyComponentShufflePayload(final GqGroup encryptionGroup, final String electionEventId, final String ballotBoxId,
			final VerifiableShuffle verifiableShuffle, final VerifiablePlaintextDecryption verifiablePlaintextDecryption) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.verifiableShuffle = checkNotNull(verifiableShuffle);
		this.verifiablePlaintextDecryption = checkNotNull(verifiablePlaintextDecryption);

		checkArgument(encryptionGroup.equals(verifiableShuffle.shuffleArgument().getGroup()),
				"The verifiable shuffle's group should be equal to the encryption group.");
		checkArgument(encryptionGroup.equals(verifiablePlaintextDecryption.getGroup()),
				"The verifiable plaintext decryption's group should be equal to the encryption group.");
		checkArgument(verifiablePlaintextDecryption.getDecryptedVotes().size() == verifiableShuffle.shuffledCiphertexts().size(),
				"The verifiable plaintext decryption and the verifiable shuffle must have the same number of ciphertexts.");
		checkArgument(verifiablePlaintextDecryption.getDecryptedVotes().getElementSize() == verifiableShuffle.shuffledCiphertexts().getElementSize(),
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

	public VerifiableShuffle getVerifiableShuffle() {
		return verifiableShuffle;
	}

	public VerifiablePlaintextDecryption getVerifiablePlaintextDecryption() {
		return verifiablePlaintextDecryption;
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
		final TallyComponentShufflePayload that = (TallyComponentShufflePayload) o;
		return encryptionGroup.equals(that.encryptionGroup) &&
				verifiableShuffle.equals(that.verifiableShuffle) &&
				verifiablePlaintextDecryption.equals(that.verifiablePlaintextDecryption) &&
				Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, verifiableShuffle, verifiablePlaintextDecryption, signature);
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(this.encryptionGroup,
				HashableString.from(this.electionEventId),
				HashableString.from(this.ballotBoxId),
				this.verifiableShuffle,
				this.verifiablePlaintextDecryption.getDecryptedVotes(),
				this.verifiablePlaintextDecryption.getDecryptionProofs());
	}

}
