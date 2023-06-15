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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.election.ControlComponentPublicKeys;
import ch.post.it.evoting.cryptoprimitives.domain.election.SetupComponentPublicKeys;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamal;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalFactory;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hash;
import ch.post.it.evoting.cryptoprimitives.hashing.HashFactory;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.SchnorrProof;

class SetupComponentPublicKeysPayloadTest {

	private static final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	private static final Hash hash = HashFactory.createHash();
	private static final GqGroup encryptionGroup = SerializationTestData.getGqGroup();
	private static final Random random = RandomFactory.createRandom();
	private static final GroupVector<SchnorrProof, ZqGroup> schnorrProofs = SerializationTestData.createSchnorrProofs(2);
	private static SetupComponentPublicKeysPayload setupComponentPublicKeysPayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setupAll() {
		final String electionEventId = random.genRandomBase16String(32).toLowerCase(Locale.ENGLISH);

		// Create payload.
		final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys = new ArrayList<>();
		IntStream.rangeClosed(1, 4).forEach(nodeId -> combinedControlComponentPublicKeys.add(generateCombinedControlComponentPublicKeys(nodeId)));

		final ElGamalMultiRecipientPublicKey electoralBoardPublicKey = SerializationTestData.getPublicKey();

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrChoiceReturnCodePublicKeys = combinedControlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::ccrjChoiceReturnCodesEncryptionPublicKey).collect(GroupVector.toGroupVector());

		final ElGamal elGamal = ElGamalFactory.createElGamal();
		final ElGamalMultiRecipientPublicKey choiceReturnCodesPublicKey = elGamal.combinePublicKeys(ccrChoiceReturnCodePublicKeys);

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccmElectionPublicKeys = Streams.concat(
				combinedControlComponentPublicKeys.stream()
						.map(ControlComponentPublicKeys::ccmjElectionPublicKey),
				Stream.of(electoralBoardPublicKey)).collect(GroupVector.toGroupVector());

		final ElGamalMultiRecipientPublicKey electionPublicKey = elGamal.combinePublicKeys(ccmElectionPublicKeys);

		final SetupComponentPublicKeys setupComponentPublicKeys = new SetupComponentPublicKeys(combinedControlComponentPublicKeys,
				electoralBoardPublicKey, schnorrProofs, electionPublicKey, choiceReturnCodesPublicKey);
		setupComponentPublicKeysPayload = new SetupComponentPublicKeysPayload(encryptionGroup, electionEventId, setupComponentPublicKeys);

		final byte[] payloadHash = hash.recursiveHash(setupComponentPublicKeysPayload);
		final CryptoPrimitivesSignature signature = new CryptoPrimitivesSignature(payloadHash);
		setupComponentPublicKeysPayload.setSignature(signature);

		// Create expected Json.
		rootNode = mapper.createObjectNode();
		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(encryptionGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		rootNode.put("electionEventId", electionEventId);

		final ObjectNode setupComponentPublicKeysNode = mapper.createObjectNode();

		final ArrayNode combinedControlComponentPublicKeysNodes = mapper.createArrayNode();

		for (final ControlComponentPublicKeys combinedControlComponentPublicKey : combinedControlComponentPublicKeys) {
			final ObjectNode combinedControlComponentPublicKeyNode = mapper.createObjectNode();
			combinedControlComponentPublicKeyNode.put("nodeId", combinedControlComponentPublicKey.nodeId());

			final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey = combinedControlComponentPublicKey.ccrjChoiceReturnCodesEncryptionPublicKey();
			final ArrayNode ccrChoiceReturnCodesEncryptionPublicKeyElements = mapper.createArrayNode();
			for (final GqElement element : ccrChoiceReturnCodesEncryptionPublicKey.getKeyElements()) {
				ccrChoiceReturnCodesEncryptionPublicKeyElements.add("0x" + element.toHashableForm());
			}

			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey = combinedControlComponentPublicKey.ccmjElectionPublicKey();
			final ArrayNode ccmElectionPublicKeyElements = mapper.createArrayNode();
			for (final GqElement element : ccmElectionPublicKey.getKeyElements()) {
				ccmElectionPublicKeyElements.add("0x" + element.toHashableForm());
			}

			// schnorrProof
			final ArrayNode schnorrProofsNodes = mapper.createArrayNode();
			final ObjectNode schnorrProofNode = mapper.createObjectNode();
			for (final SchnorrProof schnorrProof : schnorrProofs) {
				schnorrProofNode.put("_e", "0x" + schnorrProof.get_e().getValue());
				schnorrProofNode.put("_z", "0x" + schnorrProof.get_z().getValue());
				schnorrProofsNodes.add(schnorrProofNode);
			}

			combinedControlComponentPublicKeyNode.set("ccrjChoiceReturnCodesEncryptionPublicKey", ccrChoiceReturnCodesEncryptionPublicKeyElements);
			combinedControlComponentPublicKeyNode.set("ccrjSchnorrProofs", schnorrProofsNodes);
			combinedControlComponentPublicKeyNode.set("ccmjElectionPublicKey", ccmElectionPublicKeyElements);
			combinedControlComponentPublicKeyNode.set("ccmjSchnorrProofs", schnorrProofsNodes);
			combinedControlComponentPublicKeysNodes.add(combinedControlComponentPublicKeyNode);
		}

		setupComponentPublicKeysNode.set("combinedControlComponentPublicKeys", combinedControlComponentPublicKeysNodes);

		final ArrayNode electoralBoardPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : electoralBoardPublicKey.getKeyElements()) {
			electoralBoardPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		setupComponentPublicKeysNode.set("electoralBoardPublicKey", electoralBoardPublicKeyNodeElements);

		final ArrayNode schnorrProofsNodes = mapper.createArrayNode();
		final ObjectNode schnorrProofNode = mapper.createObjectNode();
		for (final SchnorrProof schnorrProof : schnorrProofs) {
			schnorrProofNode.put("_e", "0x" + schnorrProof.get_e().getValue());
			schnorrProofNode.put("_z", "0x" + schnorrProof.get_z().getValue());
			schnorrProofsNodes.add(schnorrProofNode);
		}
		setupComponentPublicKeysNode.set("electoralBoardSchnorrProofs", schnorrProofsNodes);

		final ArrayNode electionPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : electionPublicKey.getKeyElements()) {
			electionPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		setupComponentPublicKeysNode.set("electionPublicKey", electionPublicKeyNodeElements);

		final ArrayNode choiceReturnCodesPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : choiceReturnCodesPublicKey.getKeyElements()) {
			choiceReturnCodesPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		setupComponentPublicKeysNode.set("choiceReturnCodesEncryptionPublicKey", choiceReturnCodesPublicKeyNodeElements);

		rootNode.set("setupComponentPublicKeys", setupComponentPublicKeysNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(setupComponentPublicKeysPayload);
		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws IOException {
		final SetupComponentPublicKeysPayload deserializedPayload = mapper.readValue(rootNode.toString(), SetupComponentPublicKeysPayload.class);
		assertEquals(setupComponentPublicKeysPayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final SetupComponentPublicKeysPayload deserializedPayload = mapper
				.readValue(mapper.writeValueAsString(setupComponentPublicKeysPayload), SetupComponentPublicKeysPayload.class);

		assertEquals(setupComponentPublicKeysPayload, deserializedPayload);
	}

	private static ControlComponentPublicKeys generateCombinedControlComponentPublicKeys(final int nodeId) {
		final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey = SerializationTestData.getPublicKey();
		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey = SerializationTestData.getPublicKey();
		return new ControlComponentPublicKeys(nodeId, ccrChoiceReturnCodesEncryptionPublicKey, schnorrProofs, ccmElectionPublicKey, schnorrProofs);
	}
}
