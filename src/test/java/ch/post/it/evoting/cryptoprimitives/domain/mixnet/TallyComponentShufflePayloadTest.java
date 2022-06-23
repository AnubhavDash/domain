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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;

@DisplayName("A TallyComponentShufflePayload")
class TallyComponentShufflePayloadTest extends MapperSetUp {
	private static final String ELECTION_EVENT_ID = "4b7a8f063b564dbf8e24420d3f52f54f";
	private static final String BALLOT_BOX_ID = "cbf8ac1c1bcf444da0ccf5d7e956153b";
	private static final int NBR_MESSAGES = 4;
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	private static ObjectNode rootNode;
	private static TallyComponentShufflePayload tallyComponentShufflePayload;

	@BeforeAll
	static void setUpAll() throws JsonProcessingException {
		final String electionEventId = ELECTION_EVENT_ID;
		final String ballotBoxId = BALLOT_BOX_ID;

		final int nbrMessages = NBR_MESSAGES;
		final GqGroup gqGroup = SerializationTestData.getGqGroup();

		final VerifiableShuffle verifiableShuffle = SerializationTestData.getVerifiableShuffle(nbrMessages);
		final VerifiablePlaintextDecryption verifiablePlaintextDecryption = SerializationTestData.getVerifiablePlaintextDecryption(nbrMessages);

		// Generate random bytes for signature content and create payload signature.
		secureRandom.nextBytes(randomBytes);
		final CryptoPrimitivesSignature signature = new CryptoPrimitivesSignature(randomBytes);

		tallyComponentShufflePayload = new TallyComponentShufflePayload(gqGroup, electionEventId, ballotBoxId, verifiableShuffle,
				verifiablePlaintextDecryption, signature);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode electionEventIdNode = mapper.readTree(mapper.writeValueAsString(electionEventId));
		rootNode.set("electionEventId", electionEventIdNode);

		final JsonNode ballotBoxIdNode = mapper.readTree(mapper.writeValueAsString(ballotBoxId));
		rootNode.set("ballotBoxId", ballotBoxIdNode);

		final ObjectNode verifiableShuffleNode = SerializationTestData.createVerifiableShuffleNode(verifiableShuffle);
		rootNode.set("verifiableShuffle", verifiableShuffleNode);

		final ObjectNode verifiablePlaintextDecryptionNode = SerializationTestData
				.createVerifiablePlaintextDecryptionNode(verifiablePlaintextDecryption);
		rootNode.set("verifiablePlaintextDecryption", verifiablePlaintextDecryptionNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeTallyComponentShufflePayload() throws JsonProcessingException {
		final String serializedTallyComponentShufflePayload = mapper.writeValueAsString(tallyComponentShufflePayload);

		assertEquals(rootNode.toString(), serializedTallyComponentShufflePayload);
	}

	@Test
	@DisplayName("deserialized gives expected TallyComponentShufflePayload")
	void deserializeTallyComponentShufflePayload() throws IOException {
		final TallyComponentShufflePayload deserializedTallyComponentShufflePayload = mapper.readValue(rootNode.toString(),
				TallyComponentShufflePayload.class);

		assertEquals(tallyComponentShufflePayload, deserializedTallyComponentShufflePayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original TallyComponentShufflePayload")
	void cycle() throws IOException {
		final TallyComponentShufflePayload result = mapper.readValue(mapper.writeValueAsString(tallyComponentShufflePayload),
				TallyComponentShufflePayload.class);

		assertEquals(tallyComponentShufflePayload, result);
	}

	@Nested
	@DisplayName("Constructing a TallyComponentShufflePayload with")
	class TallyComponentShufflePayloadConsistencyCheckTest {

		private GqGroup encryptionGroup;
		private String electionEventId;
		private String ballotBoxId;
		private VerifiableShuffle verifiableShuffle;
		private VerifiablePlaintextDecryption verifiablePlaintextDecryption;
		private CryptoPrimitivesSignature signature;

		@BeforeEach
		void setup() {
			encryptionGroup = SerializationTestData.getGqGroup();
			electionEventId = ELECTION_EVENT_ID;
			ballotBoxId = BALLOT_BOX_ID;
			final List<ElGamalMultiRecipientCiphertext> ciphertexts = SerializationTestData.getCiphertexts(NBR_MESSAGES);

			final GroupVector<ElGamalMultiRecipientMessage, GqGroup> messages = GroupVector.from(ciphertexts.stream()
					.map(ElGamalMultiRecipientCiphertext::getPhi)
					.map(phis -> new ElGamalMultiRecipientMessage(phis))
					.toList());
			final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = SerializationTestData.getDecryptionProofs(NBR_MESSAGES);
			verifiablePlaintextDecryption = new VerifiablePlaintextDecryption(messages, decryptionProofs);

			// Generate random bytes for signature content and create payload signature.
			secureRandom.nextBytes(randomBytes);
			signature = new CryptoPrimitivesSignature(randomBytes);

			verifiableShuffle = new VerifiableShuffle(GroupVector.from(ciphertexts),
					SerializationTestData.createShuffleArgument());
		}

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void constructWithNullParameters() {
			assertAll(
					() -> assertThrows(NullPointerException.class,
							() -> new TallyComponentShufflePayload(null, electionEventId, ballotBoxId, verifiableShuffle,
									verifiablePlaintextDecryption)),
					() -> assertThrows(NullPointerException.class,
							() -> new TallyComponentShufflePayload(encryptionGroup, null, ballotBoxId, verifiableShuffle,
									verifiablePlaintextDecryption)),
					() -> assertThrows(NullPointerException.class,
							() -> new TallyComponentShufflePayload(encryptionGroup, electionEventId, null, verifiableShuffle,
									verifiablePlaintextDecryption)),
					() -> assertThrows(NullPointerException.class,
							() -> new TallyComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, null,
									verifiablePlaintextDecryption)),
					() -> assertThrows(NullPointerException.class,
							() -> new TallyComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, verifiableShuffle, null))
			);
		}

		@Test
		@DisplayName("encryption group being different from VerifiablePlaintextDecryption's or VerifiableShuffle's group throws IllegalArgumentException")
		void constructWithInconsistentGroups() {
			final VerifiablePlaintextDecryption otherVerifiablePlaintextDecryption = spy(verifiablePlaintextDecryption);
			final GqGroup otherEncryptionGroup = GroupTestData.getDifferentGqGroup(encryptionGroup);
			doReturn(otherEncryptionGroup).when(otherVerifiablePlaintextDecryption).getGroup();
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new TallyComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, verifiableShuffle,
							otherVerifiablePlaintextDecryption));
			assertEquals("The verifiable plaintext decryption's group should be equal to the encryption group.",
					Throwables.getRootCause(exception1).getMessage());

			final IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
					() -> new TallyComponentShufflePayload(otherEncryptionGroup, electionEventId, ballotBoxId, verifiableShuffle,
							otherVerifiablePlaintextDecryption));
			assertEquals("The verifiable shuffle's group should be equal to the encryption group.", Throwables.getRootCause(exception2).getMessage());
		}

		@Test
		@DisplayName("VerifiablePlaintextDecryption's and VerifiableShuffle's number of ciphertexts being different throws IllegalArgumentException")
		void constructWithInconsistentNumberCiphertexts() {
			final VerifiablePlaintextDecryption otherVerifiablePlaintextDecryption = spy(verifiablePlaintextDecryption);
			doReturn(SerializationTestData.getMessages(NBR_MESSAGES + 1)).when(otherVerifiablePlaintextDecryption).getDecryptedVotes();
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new TallyComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, verifiableShuffle,
							otherVerifiablePlaintextDecryption));
			assertEquals("The verifiable plaintext decryption and the verifiable shuffle must have the same number of ciphertexts.",
					Throwables.getRootCause(exception1).getMessage());
		}

		@Test
		@DisplayName("VerifiablePlaintextDecryption's and VerifiableShuffle's number of ciphertext elements being different throws IllegalArgumentException")
		void constructWithInconsistentNumberCiphertextElements() {
			final int l = verifiablePlaintextDecryption.getDecryptedVotes().getElementSize();
			final VerifiablePlaintextDecryption otherVerifiablePlaintextDecryption = spy(verifiablePlaintextDecryption);
			final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(encryptionGroup);
			final GroupVector<ElGamalMultiRecipientMessage, GqGroup> otherDecryptedVotes = Stream.generate(() -> gqGroupGenerator.genRandomGqElementVector(l + 1))
					.limit(NBR_MESSAGES)
					.map(ElGamalMultiRecipientMessage::new)
					.collect(GroupVector.toGroupVector());
			doReturn(otherDecryptedVotes).when(otherVerifiablePlaintextDecryption).getDecryptedVotes();
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new TallyComponentShufflePayload(encryptionGroup, electionEventId, ballotBoxId, verifiableShuffle,
							otherVerifiablePlaintextDecryption));
			assertEquals("The verifiable decryptions' and the verifiable shuffle's ciphertexts must have the same element size.",
					Throwables.getRootCause(exception1).getMessage());
		}
	}
}
