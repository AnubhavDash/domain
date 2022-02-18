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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

@JsonPropertyOrder({ "verificationCardId", "encryptedHashedSquaredConfirmationKey", "encryptedHashedSquaredPartialChoiceReturnCodes",
		"verificationCardPublicKey" })
public class ReturnCodeGenerationInput implements HashableList {

	@JsonProperty
	private final String verificationCardId;

	@JsonProperty
	private final ElGamalMultiRecipientCiphertext encryptedHashedSquaredConfirmationKey;

	@JsonProperty
	private final ElGamalMultiRecipientCiphertext encryptedHashedSquaredPartialChoiceReturnCodes;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey verificationCardPublicKey;

	/**
	 * Creates an object used as the input for return code (choice return codes and vote cast return codes) generation requests.
	 *
	 * @param verificationCardId                             the verification card identifier.
	 * @param encryptedHashedSquaredConfirmationKey          the encrypted hashed squared confirmation key.
	 * @param encryptedHashedSquaredPartialChoiceReturnCodes the encrypted hashed squared partial choice return codes.
	 * @param verificationCardPublicKey                      the verification card public key
	 */
	@JsonCreator
	public ReturnCodeGenerationInput(
			@JsonProperty("verificationCardId")
			final String verificationCardId,
			@JsonProperty("encryptedHashedSquaredConfirmationKey")
			final ElGamalMultiRecipientCiphertext encryptedHashedSquaredConfirmationKey,
			@JsonProperty("encryptedHashedSquaredPartialChoiceReturnCodes")
			final ElGamalMultiRecipientCiphertext encryptedHashedSquaredPartialChoiceReturnCodes,
			@JsonProperty("verificationCardPublicKey")
			final ElGamalMultiRecipientPublicKey verificationCardPublicKey) {

		checkNotNull(verificationCardId);
		checkNotNull(encryptedHashedSquaredConfirmationKey);
		checkNotNull(encryptedHashedSquaredPartialChoiceReturnCodes);
		checkNotNull(verificationCardPublicKey);

		this.verificationCardId = verificationCardId;
		this.encryptedHashedSquaredConfirmationKey = encryptedHashedSquaredConfirmationKey;
		this.encryptedHashedSquaredPartialChoiceReturnCodes = encryptedHashedSquaredPartialChoiceReturnCodes;
		this.verificationCardPublicKey = verificationCardPublicKey;
	}

	public String getVerificationCardId() {
		return verificationCardId;
	}

	public ElGamalMultiRecipientCiphertext getEncryptedHashedSquaredConfirmationKey() {
		return encryptedHashedSquaredConfirmationKey;
	}

	public ElGamalMultiRecipientCiphertext getEncryptedHashedSquaredPartialChoiceReturnCodes() {
		return encryptedHashedSquaredPartialChoiceReturnCodes;
	}

	public ElGamalMultiRecipientPublicKey getVerificationCardPublicKey() {
		return verificationCardPublicKey;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ReturnCodeGenerationInput that = (ReturnCodeGenerationInput) o;
		return verificationCardId.equals(that.verificationCardId) &&
				encryptedHashedSquaredConfirmationKey.equals(that.encryptedHashedSquaredConfirmationKey) &&
				encryptedHashedSquaredPartialChoiceReturnCodes.equals(that.encryptedHashedSquaredPartialChoiceReturnCodes) &&
				verificationCardPublicKey.equals(that.verificationCardPublicKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(verificationCardId, encryptedHashedSquaredConfirmationKey, encryptedHashedSquaredPartialChoiceReturnCodes,
				verificationCardPublicKey);
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(verificationCardId), encryptedHashedSquaredConfirmationKey,
				encryptedHashedSquaredPartialChoiceReturnCodes, verificationCardPublicKey);
	}

}
