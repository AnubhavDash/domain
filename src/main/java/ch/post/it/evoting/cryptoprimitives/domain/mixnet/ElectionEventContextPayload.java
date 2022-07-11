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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.election.ElectionEventContext;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * This payload represents the SetupComponentPublicKeys message
 */
@JsonDeserialize(using = ElectionEventContextPayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "electionEventContext", "signature" })
public class ElectionEventContextPayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final ElectionEventContext electionEventContext;

	@JsonProperty
	private CryptoPrimitivesSignature signature;

	@JsonCreator
	public ElectionEventContextPayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,

			@JsonProperty("electionEventContext")
			final ElectionEventContext electionEventContext,

			@JsonProperty("signature")
			final CryptoPrimitivesSignature signature
	) {

		this(encryptionGroup, electionEventContext);
		this.signature = checkNotNull(signature);
	}

	public ElectionEventContextPayload(final GqGroup encryptionGroup, final ElectionEventContext electionEventContext) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventContext = checkNotNull(electionEventContext);
		checkArgument(encryptionGroup.equals(electionEventContext.choiceReturnCodesEncryptionPublicKey().getGroup()),
				"The groups of the election event context payload and the choice return codes encryption public key of the election event context must be equal.");
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public ElectionEventContext getElectionEventContext() {
		return electionEventContext;
	}

	public CryptoPrimitivesSignature getSignature() {
		return signature;
	}

	public void setSignature(final CryptoPrimitivesSignature signature) {
		this.signature = checkNotNull(signature);
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(encryptionGroup, electionEventContext);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ElectionEventContextPayload that = (ElectionEventContextPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) &&
				electionEventContext.equals(that.electionEventContext)
				&& Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, electionEventContext, signature);
	}
}