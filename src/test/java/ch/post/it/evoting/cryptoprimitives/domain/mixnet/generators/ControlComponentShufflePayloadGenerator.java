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

package ch.post.it.evoting.cryptoprimitives.domain.mixnet.generators;

import java.security.SecureRandom;
import java.util.Random;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ControlComponentShufflePayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.VerifiableShuffleGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptionGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

public class ControlComponentShufflePayloadGenerator {

	private static final String ELECTION_EVENT_ID = "f8ba3dd3844a4815af39c63570c12006";
	private static final String BALLOT_BOX_ID = "0d31a1148f95488fae6827391425dc08";
	private static final Random secureRandom = new SecureRandom();
	private final GqGroup group;

	public ControlComponentShufflePayloadGenerator(GqGroup group) {
		this.group = group;
	}

	public ControlComponentShufflePayload genPayload(int numVotes, int voteSize, int nodeId) {

		final VerifiableShuffle verifiableShuffle = new VerifiableShuffleGenerator(group).genVerifiableShuffle(numVotes, voteSize);

		// Generate random bytes for signature content and create payload signature.
		final byte[] randomBytes = new byte[10];
		secureRandom.nextBytes(randomBytes);
		final CryptoPrimitivesSignature signature = new CryptoPrimitivesSignature(randomBytes);

		// VerifiableDecryptions.
		final VerifiableDecryptions verifiableDecryptions = new VerifiableDecryptionGenerator(group).genVerifiableDecryption(numVotes, voteSize);

		return new ControlComponentShufflePayload(group, ELECTION_EVENT_ID, BALLOT_BOX_ID, nodeId, verifiableDecryptions, verifiableShuffle, signature);
	}
}
