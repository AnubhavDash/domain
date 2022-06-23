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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

public record SetupComponentVerificationData(
		String verificationCardId,
		ElGamalMultiRecipientCiphertext encryptedHashedSquaredConfirmationKey,
		ElGamalMultiRecipientCiphertext encryptedHashedSquaredPartialChoiceReturnCodes,
		ElGamalMultiRecipientPublicKey verificationCardPublicKey) implements HashableList {

	/**
	 * Creates a record used as the input for return code (choice return codes and vote cast return codes) generation requests.
	 *
	 * @param verificationCardId                             the verification card identifier.
	 * @param encryptedHashedSquaredConfirmationKey          the encrypted hashed squared confirmation key.
	 * @param encryptedHashedSquaredPartialChoiceReturnCodes the encrypted hashed squared partial choice return codes.
	 * @param verificationCardPublicKey                      the verification card public key
	 */
	public SetupComponentVerificationData {

		validateUUID(verificationCardId);
		checkNotNull(encryptedHashedSquaredConfirmationKey);
		checkNotNull(encryptedHashedSquaredPartialChoiceReturnCodes);
		checkNotNull(verificationCardPublicKey);

		checkArgument(encryptedHashedSquaredConfirmationKey.getGroup().equals(encryptedHashedSquaredPartialChoiceReturnCodes.getGroup()),
				"The encrypted hashed squared confirmation key and the encrypted hashed squared Partial Choice Return Codes must have the same group.");
		checkArgument(encryptedHashedSquaredConfirmationKey.getGroup().equals(verificationCardPublicKey.getGroup()),
				"The encrypted hashed squared confirmation key and the verification card public key must have the same group.");
		checkArgument(encryptedHashedSquaredConfirmationKey.size() == 1, "The encrypted hashed squared confirmation key must be of size 1.");
		checkArgument(verificationCardPublicKey.size() == 1, "The verification card public key must be of size 1.");
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(HashableString.from(verificationCardId), encryptedHashedSquaredConfirmationKey, encryptedHashedSquaredPartialChoiceReturnCodes,
				verificationCardPublicKey);
	}

}
