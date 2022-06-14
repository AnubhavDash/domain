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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

class ControlComponentShufflePayloadTest extends MapperSetUp {

	private static final String ELECTION_EVENT_ID = "4b7a8f063b564dbf8e24420d3f52f54f";
	private static final String BALLOT_BOX_ID = "cbf8ac1c1bcf444da0ccf5d7e956153b";
	private static final int NBR_CIPHERTEXT = 4;
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	private static ObjectNode rootNode;
	private static ControlComponentShufflePayload controlComponentShufflePayload;

	@BeforeAll
	static void setUpAll() throws JsonProcessingException {
		final String electionEventId = ELECTION_EVENT_ID;
		final String ballotBoxId = BALLOT_BOX_ID;

		final GqGroup gqGroup = SerializationTestData.getGqGroup();

		final List<ElGamalMultiRecipientCiphertext> ciphertexts = SerializationTestData.getCiphertexts(NBR_CIPHERTEXT);

		final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = SerializationTestData.getDecryptionProofs(ciphertexts.size());
		final VerifiableDecryptions verifiableDecryptions = new VerifiableDecryptions(GroupVector.from(ciphertexts), decryptionProofs);

		// Generate random bytes for signature content and create payload signature.
		secureRandom.nextBytes(randomBytes);
		final CryptoPrimitivesSignature signature = new CryptoPrimitivesSignature(randomBytes);

		final VerifiableShuffle verifiableShuffle = new VerifiableShuffle(GroupVector.from(ciphertexts),
				SerializationTestData.createShuffleArgument());

		controlComponentShufflePayload = new ControlComponentShufflePayload(gqGroup, electionEventId, ballotBoxId, 0, verifiableDecryptions,
				verifiableShuffle, signature);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = mapper.readTree(mapper.writeValueAsString(gqGroup));
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode electionEventIdNode = mapper.readTree(mapper.writeValueAsString(electionEventId));
		rootNode.set("electionEventId", electionEventIdNode);

		final JsonNode ballotBoxIdNode = mapper.readTree(mapper.writeValueAsString(ballotBoxId));
		rootNode.set("ballotBoxId", ballotBoxIdNode);

		rootNode.put("nodeId", 0);

		final JsonNode verifiableDecryptionNode = mapper.readTree(mapper.writeValueAsString(verifiableDecryptions));
		rootNode.set("verifiableDecryptions", verifiableDecryptionNode);

		final ObjectNode verifiableShuffleNode = mapper.createObjectNode();
		final ArrayNode shuffledCiphertextsNode = SerializationTestData.createCiphertextsNode(ciphertexts);
		verifiableShuffleNode.set("shuffledCiphertexts", shuffledCiphertextsNode);
		final JsonNode jsonNode = mapper.readTree(SerializationTestData.getShuffleArgumentJson());
		verifiableShuffleNode.set("shuffleArgument", jsonNode);
		rootNode.set("verifiableShuffle", verifiableShuffleNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialize ShufflePayload gives expected json")
	void serializeShufflePayload() throws JsonProcessingException {
		final String serializedShufflePayload = mapper.writeValueAsString(controlComponentShufflePayload);

		assertEquals(rootNode.toString(), serializedShufflePayload);
	}

	@Test
	@DisplayName("deserialize ShufflePayload gives expected ShufflePayload")
	void deserializeShufflePayload() throws IOException {
		final ControlComponentShufflePayload deserializedPayload = mapper.readValue(rootNode.toString(), ControlComponentShufflePayload.class);

		assertEquals(controlComponentShufflePayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialize then deserialized gives original ShufflePayload")
	void cycle() throws IOException {
		final String serializedShufflePayload = mapper.writeValueAsString(controlComponentShufflePayload);

		final ControlComponentShufflePayload deserializedPayload = mapper.readValue(serializedShufflePayload, ControlComponentShufflePayload.class);

		assertEquals(controlComponentShufflePayload, deserializedPayload);
	}

}
