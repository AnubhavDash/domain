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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;

/**
 * Value class representing the final result of a mixnet.
 */
@JsonPropertyOrder({ "encryptionGroup", "verifiableShuffle", "verifiablePlaintextDecryption", "signature" })
@JsonDeserialize(using = TallyComponentShufflePayloadDeserializer.class)
public class TallyComponentShufflePayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final VerifiableShuffle verifiableShuffle;

	@JsonProperty
	private final VerifiablePlaintextDecryption verifiablePlaintextDecryption;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public TallyComponentShufflePayload(
			@JsonProperty(value = "encryptionGroup", required = true)
			final GqGroup encryptionGroup,
			@JsonProperty(value = "verifiableShuffle", required = true)
			final VerifiableShuffle verifiableShuffle,
			@JsonProperty(value = "verifiablePlaintextDecryption", required = true)
			final VerifiablePlaintextDecryption verifiablePlaintextDecryption,
			@JsonProperty(value = "signature", required = true)
			final CryptoPrimitivesPayloadSignature signature) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.verifiableShuffle = checkNotNull(verifiableShuffle);
		this.verifiablePlaintextDecryption = checkNotNull(verifiablePlaintextDecryption);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Constructs an unsigned payload.
	 */
	public TallyComponentShufflePayload(final GqGroup encryptionGroup, final VerifiableShuffle verifiableShuffle,
			final VerifiablePlaintextDecryption verifiablePlaintextDecryption) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.verifiableShuffle = checkNotNull(verifiableShuffle);
		this.verifiablePlaintextDecryption = checkNotNull(verifiablePlaintextDecryption);
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

	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

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
		final TallyComponentShufflePayload that = (TallyComponentShufflePayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && verifiableShuffle.equals(that.verifiableShuffle)
				&& verifiablePlaintextDecryption.equals(
				that.verifiablePlaintextDecryption) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, verifiableShuffle, verifiablePlaintextDecryption, signature);
	}

	@Override
	public List<? extends Hashable> toHashableForm() {
		return List.of(this.encryptionGroup, this.verifiableShuffle, this.verifiablePlaintextDecryption.getDecryptedVotes(),
				this.verifiablePlaintextDecryption.getDecryptionProofs());
	}
}
