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
import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

class MixnetShufflePayloadTest extends MapperSetUp {

	private static final String ELECTION_EVENT_ID = "4b7a8f063b564dbf8e24420d3f52f54f";
	private static final String BALLOT_BOX_ID = "cbf8ac1c1bcf444da0ccf5d7e956153b";
	private static final int NBR_CIPHERTEXT = 4;
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	private static String electionEventId;
	private static String ballotBoxId;
	private static List<ElGamalMultiRecipientCiphertext> ciphertexts;
	private static VerifiableDecryptions verifiableDecryptions;
	private static ElGamalMultiRecipientPublicKey remainingElectionPublicKey;
	private static ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey;
	private static ElGamalMultiRecipientPublicKey nodeElectionPublicKey;
	private static CryptoPrimitivesPayloadSignature signature;
	private static ObjectNode rootNode;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() throws JsonProcessingException {
		electionEventId = ELECTION_EVENT_ID;
		ballotBoxId = BALLOT_BOX_ID;

		gqGroup = SerializationTestData.getGqGroup();

		ciphertexts = SerializationTestData.getCiphertexts(NBR_CIPHERTEXT);

		final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = SerializationTestData.getDecryptionProofs(ciphertexts.size());
		verifiableDecryptions = new VerifiableDecryptions(GroupVector.from(ciphertexts), decryptionProofs);

		remainingElectionPublicKey = SerializationTestData.getPublicKey();
		previousRemainingElectionPublicKey = SerializationTestData.getPublicKey();
		nodeElectionPublicKey = SerializationTestData.getPublicKey();

		// Generate random bytes for signature content and create payload signature.
		secureRandom.nextBytes(randomBytes);
		final X509Certificate certificate = SerializationTestData.generateTestCertificate();
		signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final JsonNode electionEventIdNode = mapper.readTree(mapper.writeValueAsString(electionEventId));
		rootNode.set("electionEventId", electionEventIdNode);

		final JsonNode ballotBoxIdNode = mapper.readTree(mapper.writeValueAsString(ballotBoxId));
		rootNode.set("ballotBoxId", ballotBoxIdNode);

		final JsonNode encryptionGroupNode = mapper.readTree(mapper.writeValueAsString(gqGroup));
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode verifiableDecryptionNode = mapper.readTree(mapper.writeValueAsString(verifiableDecryptions));
		rootNode.set("verifiableDecryptions", verifiableDecryptionNode);

		final ObjectNode verifiableShuffleNode = mapper.createObjectNode();
		final ArrayNode shuffledCiphertextsNode = SerializationTestData.createCiphertextsNode(ciphertexts);
		verifiableShuffleNode.set("shuffledCiphertexts", shuffledCiphertextsNode);
		final JsonNode jsonNode = mapper.readTree(SerializationTestData.getShuffleArgumentJson());
		verifiableShuffleNode.set("shuffleArgument", jsonNode);
		rootNode.set("verifiableShuffle", verifiableShuffleNode);

		final ArrayNode remainingElectionPublicKeyNode = SerializationTestData.createPublicKeyNode(remainingElectionPublicKey);
		rootNode.set("remainingElectionPublicKey", remainingElectionPublicKeyNode);
		final ArrayNode previousRemainingElectionPublicKeyNode = SerializationTestData.createPublicKeyNode(previousRemainingElectionPublicKey);
		rootNode.set("previousRemainingElectionPublicKey", previousRemainingElectionPublicKeyNode);
		final ArrayNode nodeElectionPublicKeyNode = SerializationTestData.createPublicKeyNode(nodeElectionPublicKey);
		rootNode.set("nodeElectionPublicKey", nodeElectionPublicKeyNode);

		rootNode.put("nodeId", 0);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Nested
	@DisplayName("with VerifiableShuffle")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class WithVerifiableShuffle {

		private MixnetShufflePayload mixnetShufflePayload;

		@BeforeAll
		void setUp() {
			final VerifiableShuffle verifiableShuffle = new VerifiableShuffle(GroupVector.from(ciphertexts),
					SerializationTestData.createShuffleArgument());

			mixnetShufflePayload = new MixnetShufflePayload(electionEventId, ballotBoxId, gqGroup, verifiableDecryptions, verifiableShuffle, remainingElectionPublicKey,
					previousRemainingElectionPublicKey, nodeElectionPublicKey, 0, signature);
		}

		@Test
		@DisplayName("serialize ShufflePayload gives expected json")
		void serializeShufflePayload() throws JsonProcessingException {
			final String serializedShufflePayload = mapper.writeValueAsString(mixnetShufflePayload);

			assertEquals(rootNode.toString(), serializedShufflePayload);
		}

		@Test
		@DisplayName("deserialize ShufflePayload gives expected ShufflePayload")
		void deserializeShufflePayload() throws IOException {
			final MixnetShufflePayload deserializedPayload = mapper.readValue(rootNode.toString(), MixnetShufflePayload.class);

			assertEquals(mixnetShufflePayload, deserializedPayload);
		}

		@Test
		@DisplayName("serialize then deserialized gives original ShufflePayload")
		void cycle() throws IOException {
			final String serializedShufflePayload = mapper.writeValueAsString(mixnetShufflePayload);

			final MixnetShufflePayload deserializedPayload = mapper.readValue(serializedShufflePayload, MixnetShufflePayload.class);

			assertEquals(mixnetShufflePayload, deserializedPayload);
		}

	}

	@Nested
	@DisplayName("without VerifiableShuffle")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class WithoutVerifiableShuffle {

		private MixnetShufflePayload payloadWithoutVerifiableShuffle;
		private ObjectNode rootNodeCopy;

		@BeforeAll
		void setUpAll() {
			payloadWithoutVerifiableShuffle = new MixnetShufflePayload(electionEventId, ballotBoxId, gqGroup, verifiableDecryptions, null, remainingElectionPublicKey,
					previousRemainingElectionPublicKey, nodeElectionPublicKey, 0, signature);

			rootNodeCopy = rootNode.deepCopy();
			rootNodeCopy.remove("verifiableShuffle");
		}

		@Test
		@DisplayName("serialized gives expected json")
		void serializeWithoutVerifiableShuffle() throws JsonProcessingException {
			final String serializedShufflePayload = mapper.writeValueAsString(payloadWithoutVerifiableShuffle);

			assertEquals(rootNodeCopy.toString(), serializedShufflePayload);
		}

		@Test
		@DisplayName("deserialized gives expected ShufflePayload")
		void deserializeShufflePayload() throws IOException {
			final MixnetShufflePayload deserializedPayload = mapper.readValue(rootNodeCopy.toString(), MixnetShufflePayload.class);

			assertEquals(payloadWithoutVerifiableShuffle, deserializedPayload);
		}

		@Test
		@DisplayName("serialized then deserialized gives original ShufflePayload")
		void cycle() throws IOException {
			final String serializedShufflePayload = mapper.writeValueAsString(payloadWithoutVerifiableShuffle);

			final MixnetShufflePayload deserializedPayload = mapper.readValue(serializedShufflePayload, MixnetShufflePayload.class);

			assertEquals(payloadWithoutVerifiableShuffle, deserializedPayload);
		}

	}

}
