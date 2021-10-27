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

package ch.post.it.evoting.cryptoprimitives.domain.mixnet.generators;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Random;

import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffleGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptionGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

public class MixnetShufflePayloadGenerator {

	private static final Random secureRandom = new SecureRandom();
	private final GqGroup group;

	public MixnetShufflePayloadGenerator(GqGroup group) {
		this.group = group;
	}

	public MixnetShufflePayload genPayload(int numVotes, int voteSize, int nodeId) {

		final VerifiableShuffle verifiableShuffle =
				numVotes <= 1 ? null : new VerifiableShuffleGenerator(group).genVerifiableShuffle(numVotes, voteSize);

		ElGamalGenerator generator = new ElGamalGenerator(group);
		final ElGamalMultiRecipientPublicKey remainingElectionPublicKey = generator.genRandomPublicKey(voteSize);
		final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey = generator.genRandomPublicKey(voteSize);
		final ElGamalMultiRecipientPublicKey nodeElectionPublicKey = generator.genRandomPublicKey(voteSize);

		// Generate random bytes for signature content and create payload signature.
		final byte[] randomBytes = new byte[10];
		secureRandom.nextBytes(randomBytes);
		final X509Certificate certificate = SerializationTestData.generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });

		// VerifiableDecryptions.
		final VerifiableDecryptions verifiableDecryptions = new VerifiableDecryptionGenerator(group).genVerifiableDecryption(numVotes, voteSize);

		return new MixnetShufflePayload(group, verifiableDecryptions, verifiableShuffle, remainingElectionPublicKey,
				previousRemainingElectionPublicKey, nodeElectionPublicKey, nodeId, signature);
	}
}
