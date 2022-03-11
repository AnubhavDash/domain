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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;

/**
 * Value class representing the final result of a mixnet.
 */
@JsonPropertyOrder({ "encryptionGroup", "verifiableShuffle", "verifiablePlaintextDecryption", "previousRemainingElectionPublicKey" })
@JsonDeserialize(using = MixnetFinalPayloadDeserializer.class)
public class MixnetFinalPayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	private final VerifiableShuffle verifiableShuffle;

	@JsonProperty
	private final VerifiablePlaintextDecryption verifiablePlaintextDecryption;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public MixnetFinalPayload(
			@JsonProperty(value = "encryptionGroup", required = true)
			final GqGroup encryptionGroup,
			@JsonProperty("verifiableShuffle")
			final VerifiableShuffle verifiableShuffle,
			@JsonProperty(value = "verifiablePlaintextDecryption", required = true)
			final VerifiablePlaintextDecryption verifiablePlaintextDecryption,
			@JsonProperty(value = "previousRemainingElectionPublicKey", required = true)
			final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey,
			@JsonProperty(value = "signature", required = true)
			final CryptoPrimitivesPayloadSignature signature) {

		checkNotNull(encryptionGroup);
		checkNotNull(verifiablePlaintextDecryption);
		checkNotNull(previousRemainingElectionPublicKey);
		checkNotNull(signature);

		this.encryptionGroup = encryptionGroup;
		this.verifiableShuffle = verifiableShuffle;
		this.verifiablePlaintextDecryption = verifiablePlaintextDecryption;
		this.previousRemainingElectionPublicKey = previousRemainingElectionPublicKey;
		this.signature = signature;
	}

	/**
	 * Constructs an unsigned payload.
	 */
	public MixnetFinalPayload(final GqGroup encryptionGroup, final VerifiableShuffle verifiableShuffle,
			final VerifiablePlaintextDecryption verifiablePlaintextDecryption,
			final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey) {

		checkNotNull(encryptionGroup);
		checkNotNull(verifiablePlaintextDecryption);
		checkNotNull(previousRemainingElectionPublicKey);

		this.encryptionGroup = encryptionGroup;
		this.verifiableShuffle = verifiableShuffle;
		this.verifiablePlaintextDecryption = verifiablePlaintextDecryption;
		this.previousRemainingElectionPublicKey = previousRemainingElectionPublicKey;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	@JsonIgnore
	public Optional<VerifiableShuffle> getVerifiableShuffle() {
		return Optional.ofNullable(verifiableShuffle);
	}

	public VerifiablePlaintextDecryption getVerifiablePlaintextDecryption() {
		return verifiablePlaintextDecryption;
	}

	public ElGamalMultiRecipientPublicKey getPreviousRemainingElectionPublicKey() {
		return previousRemainingElectionPublicKey;
	}

	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	/**
	 * @param signature must be not null
	 */
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
		final MixnetFinalPayload that = (MixnetFinalPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && Objects.equals(verifiableShuffle, that.verifiableShuffle)
				&& verifiablePlaintextDecryption.equals(that.verifiablePlaintextDecryption) && previousRemainingElectionPublicKey
				.equals(that.previousRemainingElectionPublicKey) && signature.equals(that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, verifiableShuffle, verifiablePlaintextDecryption, previousRemainingElectionPublicKey, signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		if (this.verifiableShuffle != null) {
			return ImmutableList.of(this.encryptionGroup, this.verifiableShuffle, this.verifiablePlaintextDecryption.getDecryptedVotes(),
					this.verifiablePlaintextDecryption.getDecryptionProofs(), this.previousRemainingElectionPublicKey);
		} else {
			return ImmutableList.of(this.encryptionGroup, this.verifiablePlaintextDecryption.getDecryptedVotes(),
					this.verifiablePlaintextDecryption.getDecryptionProofs(), this.previousRemainingElectionPublicKey);
		}
	}
}
