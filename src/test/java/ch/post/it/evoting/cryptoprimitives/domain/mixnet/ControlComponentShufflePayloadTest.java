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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
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

		controlComponentShufflePayload = new ControlComponentShufflePayload(gqGroup, electionEventId, ballotBoxId, 1, verifiableDecryptions,
				verifiableShuffle, signature);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = mapper.readTree(mapper.writeValueAsString(gqGroup));
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode electionEventIdNode = mapper.readTree(mapper.writeValueAsString(electionEventId));
		rootNode.set("electionEventId", electionEventIdNode);

		final JsonNode ballotBoxIdNode = mapper.readTree(mapper.writeValueAsString(ballotBoxId));
		rootNode.set("ballotBoxId", ballotBoxIdNode);

		rootNode.put("nodeId", 1);

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

	@Nested
	@DisplayName("Constructing a ControlComponentShufflePayload with")
	class ControlComponentShufflePayloadConsistencyCheckTest {

		private GqGroup encryptionGroup;
		private String electionEventId;
		private String ballotBoxId;
		private int nodeId;
		private VerifiableDecryptions verifiableDecryptions;
		private VerifiableShuffle verifiableShuffle;

		@BeforeEach
		void setup() {
			encryptionGroup = SerializationTestData.getGqGroup();
			electionEventId = ELECTION_EVENT_ID;
			ballotBoxId = BALLOT_BOX_ID;
			nodeId = secureRandom.nextInt(4) + 1;
			final List<ElGamalMultiRecipientCiphertext> ciphertexts = SerializationTestData.getCiphertexts(NBR_CIPHERTEXT);

			final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = SerializationTestData.getDecryptionProofs(ciphertexts.size());
			verifiableDecryptions = new VerifiableDecryptions(GroupVector.from(ciphertexts), decryptionProofs);

			verifiableShuffle = new VerifiableShuffle(GroupVector.from(ciphertexts),
					SerializationTestData.createShuffleArgument());
		}

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void constructWithNullParameters() {
			assertAll(
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentShufflePayload(null, electionEventId, ballotBoxId, nodeId, verifiableDecryptions,
									verifiableShuffle)),
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentShufflePayload(encryptionGroup, null, ballotBoxId, nodeId, verifiableDecryptions,
									verifiableShuffle)),
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, null, nodeId, verifiableDecryptions,
									verifiableShuffle)),
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, nodeId, null, verifiableShuffle)),
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, nodeId, verifiableDecryptions,
									null))
			);
		}

		@Test
		@DisplayName("nodeId out of range throws IllegalArgumentException")
		void constructWithBadNodeId() {
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, 0, verifiableDecryptions,
							verifiableShuffle));
			assertEquals(String.format("The node id must be part of the known node ids. [nodeId: %s]", 0),
					Throwables.getRootCause(exception1).getMessage());

			final IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, 5, verifiableDecryptions,
							verifiableShuffle));
			assertEquals(String.format("The node id must be part of the known node ids. [nodeId: %s]", 5),
					Throwables.getRootCause(exception2).getMessage());
		}

		@Test
		@DisplayName("encryption group being different from VerifiableDecryptions' or VerifiableShuffle's group throws IllegalArgumentException")
		void constructWithInconsistentGroups() {
			final VerifiableDecryptions otherVerifiableDecryptions = spy(verifiableDecryptions);
			final GqGroup otherEncryptionGroup = GroupTestData.getDifferentGqGroup(encryptionGroup);
			doReturn(otherEncryptionGroup).when(otherVerifiableDecryptions).getGroup();
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, nodeId, otherVerifiableDecryptions,
							verifiableShuffle));
			assertEquals("The verifiable decryptions' group should be equal to the encryption group.", Throwables.getRootCause(exception1).getMessage());

			final IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentShufflePayload(otherEncryptionGroup, electionEventId, ballotBoxId, nodeId, otherVerifiableDecryptions,
							verifiableShuffle));
			assertEquals("The verifiable shuffle's group should be equal to the encryption group.", Throwables.getRootCause(exception2).getMessage());
		}

		@Test
		@DisplayName("VerifiableDecryptions' and VerifiableShuffle's number of ciphertexts being different throws IllegalArgumentException")
		void constructWithInconsistentNumberCiphertexts() {
			final VerifiableDecryptions otherVerifiableDecryptions = spy(verifiableDecryptions);
			doReturn(verifiableDecryptions.get_N() + 1).when(otherVerifiableDecryptions).get_N();
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, nodeId, otherVerifiableDecryptions,
							verifiableShuffle));
			assertEquals("The verifiable decryptions and the verifiable shuffle must have the same number of ciphertexts.", Throwables.getRootCause(exception1).getMessage());
		}

		@Test
		@DisplayName("VerifiableDecryptions' and VerifiableShuffle's number of ciphertext elements being different throws IllegalArgumentException")
		void constructWithInconsistentNumberCiphertextElements() {
			final VerifiableDecryptions otherVerifiableDecryptions = spy(verifiableDecryptions);
			doReturn(verifiableDecryptions.get_l() + 1).when(otherVerifiableDecryptions).get_l();
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, nodeId, otherVerifiableDecryptions,
							verifiableShuffle));
			assertEquals("The verifiable decryptions' and the verifiable shuffle's ciphertexts must have the same element size.", Throwables.getRootCause(exception1).getMessage());
		}
	}
}
