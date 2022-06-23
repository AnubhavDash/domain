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
import java.util.function.Function;
import java.util.stream.Stream;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.utils.Validations;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;

/**
 * Encapsulates a control component's output when generating return codes (both choice return codes and vote cast return codes) in the configuration
 * phase - namely in the algorithm GenEncLongCodeShares.
 */
public record ControlComponentCodeShare(
		String verificationCardId,
		ElGamalMultiRecipientPublicKey voterChoiceReturnCodeGenerationPublicKey,
		ElGamalMultiRecipientPublicKey voterVoteCastReturnCodeGenerationPublicKey,

		/* The squared, hashed partial Choice Return Codes that were
		 * exponentiated to the Voter Choice Return Code Generation Private key and encrypted with the setup public key.
		 */
		ElGamalMultiRecipientCiphertext exponentiatedEncryptedPartialChoiceReturnCodes,

		/* Proof that the partial choice return code - hashed, squared, and encrypted with the setup public key - was exponentiated to the
		 * Voter Choice Return Code generation secret key.
		 */
		ExponentiationProof encryptedPartialChoiceReturnCodeExponentiationProof,

		/* The squared, hashed partial Choice Return Codes that were
		 * exponentiated to the Voter Choice Return Code Generation Private key and encrypted with the setup public key.
		 */
		ElGamalMultiRecipientCiphertext exponentiatedEncryptedConfirmationKey,

		/* Proof that the confirmation key - hashed, squared, and encrypted with the setup public key - was exponentiated to the Voter Vote
		 * Cast Return Code generation private key.
		 */
		ExponentiationProof encryptedConfirmationKeyExponentiationProof) implements HashableList {

	public ControlComponentCodeShare {
		validateUUID(verificationCardId);
		checkNotNull(voterChoiceReturnCodeGenerationPublicKey);
		checkNotNull(voterVoteCastReturnCodeGenerationPublicKey);
		checkNotNull(exponentiatedEncryptedPartialChoiceReturnCodes);
		checkNotNull(encryptedPartialChoiceReturnCodeExponentiationProof);
		checkNotNull(exponentiatedEncryptedConfirmationKey);
		checkNotNull(encryptedConfirmationKeyExponentiationProof);

		checkArgument(Validations.allEqual(Stream.of(voterChoiceReturnCodeGenerationPublicKey.getGroup(),
						voterVoteCastReturnCodeGenerationPublicKey.getGroup(), exponentiatedEncryptedPartialChoiceReturnCodes.getGroup(),
						exponentiatedEncryptedConfirmationKey.getGroup()), Function.identity()),
				"The keys and partial choice return codes must have the same group.");
		checkArgument(
				encryptedPartialChoiceReturnCodeExponentiationProof.getGroup().hasSameOrderAs(exponentiatedEncryptedConfirmationKey.getGroup()),
				"The Encrypted Partial Choice Return Code Exponentiation Proofs must have the same group order as the keys.");
		checkArgument(voterChoiceReturnCodeGenerationPublicKey.size() == 1, "The voter Choice Return Code Generation Public Key must be of size 1.");
		checkArgument(voterVoteCastReturnCodeGenerationPublicKey.size() == 1,
				"The voter Vote Cast Return Code Generation Public Key must be of size 1.");
		checkArgument(exponentiatedEncryptedConfirmationKey.size() == 1, "The exponentiated Encrypted Confirmation Key must be of size 1.");
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(
				HashableString.from(verificationCardId),
				voterChoiceReturnCodeGenerationPublicKey,
				voterVoteCastReturnCodeGenerationPublicKey,
				exponentiatedEncryptedPartialChoiceReturnCodes,
				encryptedPartialChoiceReturnCodeExponentiationProof,
				exponentiatedEncryptedConfirmationKey,
				encryptedConfirmationKeyExponentiationProof);
	}

}
