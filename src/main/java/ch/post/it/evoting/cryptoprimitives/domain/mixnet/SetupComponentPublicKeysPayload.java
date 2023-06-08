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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.election.SetupComponentPublicKeys;
import ch.post.it.evoting.cryptoprimitives.domain.signature.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonDeserialize(using = SetupComponentPublicKeysPayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "electionEventId", "setupComponentPublicKeys", "signature" })
public class SetupComponentPublicKeysPayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final SetupComponentPublicKeys setupComponentPublicKeys;

	@JsonProperty
	private CryptoPrimitivesSignature signature;

	@JsonCreator
	public SetupComponentPublicKeysPayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("electionEventId")
			final String electionEventId,
			@JsonProperty("setupComponentPublicKeys")
			final SetupComponentPublicKeys setupComponentPublicKeys,
			@JsonProperty("signature")
			final CryptoPrimitivesSignature signature) {
		this(encryptionGroup, electionEventId, setupComponentPublicKeys);
		this.signature = checkNotNull(signature);
	}

	public SetupComponentPublicKeysPayload(
			final GqGroup encryptionGroup,
			final String electionEventId,
			final SetupComponentPublicKeys setupComponentPublicKeys
	) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventId = checkNotNull(electionEventId);
		this.setupComponentPublicKeys = checkNotNull(setupComponentPublicKeys);
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public SetupComponentPublicKeys getSetupComponentPublicKeys() {
		return setupComponentPublicKeys;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SetupComponentPublicKeysPayload that = (SetupComponentPublicKeysPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && setupComponentPublicKeys.equals(that.setupComponentPublicKeys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, setupComponentPublicKeys);
	}

	@Override
	public CryptoPrimitivesSignature getSignature() {
		return this.signature;
	}

	@Override
	public void setSignature(CryptoPrimitivesSignature signature) {
		this.signature = signature;
	}

	@Override
	public List<? extends Hashable> toHashableForm() {
		return List.of(encryptionGroup, setupComponentPublicKeys);
	}
}
