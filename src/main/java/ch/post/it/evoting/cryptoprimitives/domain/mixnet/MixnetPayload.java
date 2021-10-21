/*
 * Copyright 2021 Post CH Ltd
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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;

/**
 * Represents a mixnet payload. This payload is the input or the output of a mixing / decryption operation.
 */
@JsonDeserialize(using = MixnetPayloadDeserializer.class)
public interface MixnetPayload {

	List<ElGamalMultiRecipientCiphertext> getEncryptedVotes();

	ElGamalMultiRecipientPublicKey getRemainingElectionPublicKey();

	CryptoPrimitivesPayloadSignature getSignature();

	void setSignature(final CryptoPrimitivesPayloadSignature signature);
}
