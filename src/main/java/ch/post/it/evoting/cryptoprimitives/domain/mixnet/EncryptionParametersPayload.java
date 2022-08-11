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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;

@JsonDeserialize(using = EncryptionParametersPayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "seed", "smallPrimes", "signature" })
public class EncryptionParametersPayload implements SignedPayload {
	@JsonProperty
	private final GqGroup encryptionGroup;
	@JsonProperty
	private final String seed;
	@JsonProperty
	private final GroupVector<PrimeGqElement, GqGroup> smallPrimes;
	@JsonProperty
	private CryptoPrimitivesSignature signature;

	@JsonCreator
	public EncryptionParametersPayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("seed")
			final String seed,
			@JsonProperty("smallPrimes")
			final GroupVector<PrimeGqElement, GqGroup> smallPrimes,
			@JsonProperty("signature")
			final CryptoPrimitivesSignature signature) {
		this(encryptionGroup, seed, smallPrimes);
		this.signature = checkNotNull(signature);
	}

	public EncryptionParametersPayload(final GqGroup encryptionGroup, final String seed,
			final GroupVector<PrimeGqElement, GqGroup> smallPrimes) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.seed = checkNotNull(seed);
		checkArgument(!seed.isEmpty());
		this.smallPrimes = checkNotNull(smallPrimes);
		checkArgument(!smallPrimes.isEmpty(), "The smallPrimes must not be empty.");
		checkArgument(encryptionGroup.equals(smallPrimes.getGroup()),
				"The groups of the smallPrimes and the encryptionGroup must be equal.");

		// check for ascending order of the primes
		final boolean isStrictlyAscending = IntStream.range(0, smallPrimes.size() - 1)
				.allMatch(i -> smallPrimes.get(i).getValue().compareTo(smallPrimes.get(i + 1).getValue()) < 0);
		checkArgument(isStrictlyAscending, "The elements of smallPrimes must be in ascending order.");
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public String getSeed() {
		return seed;
	}

	public GroupVector<PrimeGqElement, GqGroup> getSmallPrimes() {
		return smallPrimes;
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
		return List.of(
				HashableList.from(encryptionGroup.toHashableForm()),
				HashableString.from(seed),
				HashableList.from(smallPrimes));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final EncryptionParametersPayload that = (EncryptionParametersPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) &&
				seed.equals(that.seed) &&
				smallPrimes.equals(that.smallPrimes) &&
				Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, seed, smallPrimes, signature);
	}
}