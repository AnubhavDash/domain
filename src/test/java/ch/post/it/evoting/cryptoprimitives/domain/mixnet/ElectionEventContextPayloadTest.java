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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import ch.post.it.evoting.cryptoprimitives.domain.election.ElectionEventContext;
import ch.post.it.evoting.cryptoprimitives.domain.election.PrimesMappingTable;
import ch.post.it.evoting.cryptoprimitives.domain.election.PrimesMappingTableEntry;
import ch.post.it.evoting.cryptoprimitives.domain.election.VerificationCardSetContext;
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
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.SchnorrProof;

class ElectionEventContextPayloadTest {

	private static final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	private static final Hash hash = HashFactory.createHash();
	private static final GqGroup encryptionGroup = SerializationTestData.getGqGroup();
	private static final Random random = RandomFactory.createRandom();
	private static final GroupVector<PrimeGqElement, GqGroup> smallPrimeGroupMembers = PrimeGqElement.PrimeGqElementFactory.getSmallPrimeGroupMembers(
			encryptionGroup, 1);
	private static final GroupVector<SchnorrProof, ZqGroup> schnorrProofs = SerializationTestData.createSchnorrProofs(5);
	private static ElectionEventContextPayload electionEventContextPayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setupAll() throws JsonProcessingException {

		// Create payload.
		final String electionEventId = random.genRandomBase16String(32).toLowerCase();
		final List<VerificationCardSetContext> verificationCardSetContexts = new ArrayList<>();

		final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys = new ArrayList<>();

		IntStream.rangeClosed(1, 2).forEach(i -> verificationCardSetContexts.add(generatedVerificationCardSetContext()));

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

		final LocalDateTime startTime = LocalDateTime.now();
		final LocalDateTime finishTime = startTime.plusWeeks(1);

		final ElectionEventContext electionEventContext = new ElectionEventContext(electionEventId, verificationCardSetContexts,
				combinedControlComponentPublicKeys, electoralBoardPublicKey, schnorrProofs, electionPublicKey, choiceReturnCodesPublicKey, startTime,
				finishTime);

		electionEventContextPayload = new ElectionEventContextPayload(encryptionGroup, electionEventContext);

		final byte[] payloadHash = hash.recursiveHash(electionEventContextPayload);

		final CryptoPrimitivesSignature signature = new CryptoPrimitivesSignature(payloadHash);
		electionEventContextPayload.setSignature(signature);

		// Create expected Json.
		rootNode = mapper.createObjectNode();
		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(encryptionGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ObjectNode electionEventContextNode = mapper.createObjectNode();
		electionEventContextNode.put("electionEventId", electionEventId);

		final ArrayNode verificationCardSetContextsNodes = mapper.createArrayNode();
		for (final VerificationCardSetContext verificationCardSetContext : verificationCardSetContexts) {
			final ObjectNode verificationCardSetContextNode = mapper.createObjectNode();
			verificationCardSetContextNode.put("verificationCardSetId", verificationCardSetContext.verificationCardSetId());
			verificationCardSetContextNode.put("ballotBoxId", verificationCardSetContext.ballotBoxId());
			verificationCardSetContextNode.put("testBallotBox", verificationCardSetContext.testBallotBox());
			verificationCardSetContextNode.put("numberOfWriteInFields", verificationCardSetContext.numberOfWriteInFields());
			verificationCardSetContextNode.put("numberOfVotingCards", verificationCardSetContext.numberOfVotingCards());
			verificationCardSetContextNode.put("gracePeriod", verificationCardSetContext.gracePeriod());
			verificationCardSetContextNode.set("primesMappingTable",
					mapper.readTree(mapper.writeValueAsString(verificationCardSetContext.primesMappingTable())));
			verificationCardSetContextsNodes.add(verificationCardSetContextNode);
		}
		electionEventContextNode.set("verificationCardSetContexts", verificationCardSetContextsNodes);

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

		electionEventContextNode.set("combinedControlComponentPublicKeys", combinedControlComponentPublicKeysNodes);

		final ArrayNode electoralBoardPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : electoralBoardPublicKey.getKeyElements()) {
			electoralBoardPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		electionEventContextNode.set("electoralBoardPublicKey", electoralBoardPublicKeyNodeElements);

		final ArrayNode schnorrProofsNodes = mapper.createArrayNode();
		final ObjectNode schnorrProofNode = mapper.createObjectNode();
		for (final SchnorrProof schnorrProof : schnorrProofs) {
			schnorrProofNode.put("_e", "0x" + schnorrProof.get_e().getValue());
			schnorrProofNode.put("_z", "0x" + schnorrProof.get_z().getValue());
			schnorrProofsNodes.add(schnorrProofNode);
		}
		electionEventContextNode.set("electoralBoardSchnorrProofs", schnorrProofsNodes);

		final ArrayNode electionPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : electionPublicKey.getKeyElements()) {
			electionPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		electionEventContextNode.set("electionPublicKey", electionPublicKeyNodeElements);

		final ArrayNode choiceReturnCodesPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : choiceReturnCodesPublicKey.getKeyElements()) {
			choiceReturnCodesPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		electionEventContextNode.set("choiceReturnCodesEncryptionPublicKey", choiceReturnCodesPublicKeyNodeElements);

		final ArrayNode startTimeNodeElements = mapper.createArrayNode();
		startTimeNodeElements.add(startTime.getYear());
		startTimeNodeElements.add(startTime.getMonthValue());
		startTimeNodeElements.add(startTime.getDayOfMonth());
		startTimeNodeElements.add(startTime.getHour());
		startTimeNodeElements.add(startTime.getMinute());
		startTimeNodeElements.add(startTime.getSecond());
		startTimeNodeElements.add(startTime.getNano());
		electionEventContextNode.set("startTime", startTimeNodeElements);

		final ArrayNode finishTimeNodeElements = mapper.createArrayNode();
		finishTimeNodeElements.add(finishTime.getYear());
		finishTimeNodeElements.add(finishTime.getMonthValue());
		finishTimeNodeElements.add(finishTime.getDayOfMonth());
		finishTimeNodeElements.add(finishTime.getHour());
		finishTimeNodeElements.add(finishTime.getMinute());
		finishTimeNodeElements.add(finishTime.getSecond());
		finishTimeNodeElements.add(finishTime.getNano());
		electionEventContextNode.set("finishTime", finishTimeNodeElements);

		rootNode.set("electionEventContext", electionEventContextNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(electionEventContextPayload);
		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws IOException {
		final ElectionEventContextPayload deserializedPayload = mapper.readValue(rootNode.toString(), ElectionEventContextPayload.class);
		assertEquals(electionEventContextPayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final ElectionEventContextPayload deserializedPayload = mapper
				.readValue(mapper.writeValueAsString(electionEventContextPayload), ElectionEventContextPayload.class);

		assertEquals(electionEventContextPayload, deserializedPayload);
	}

	private static ControlComponentPublicKeys generateCombinedControlComponentPublicKeys(final int nodeId) {
		final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey = SerializationTestData.getPublicKey();
		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey = SerializationTestData.getPublicKey();
		return new ControlComponentPublicKeys(nodeId, ccrChoiceReturnCodesEncryptionPublicKey, schnorrProofs, ccmElectionPublicKey, schnorrProofs);
	}

	private static VerificationCardSetContext generatedVerificationCardSetContext() {
		final String verificationCardSetId = random.genRandomBase16String(32).toLowerCase();
		final String ballotBoxId = random.genRandomBase16String(32).toLowerCase();
		final boolean testBallotBox = Math.random() < 0.5;
		final int numberOfWriteInFields = 1;
		final int numberOfVotingCards = 10;
		final int gracePeriod = 900;
		final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(
				List.of(new PrimesMappingTableEntry("actualVotingOption", smallPrimeGroupMembers.get(0))));

		return new VerificationCardSetContext(verificationCardSetId, ballotBoxId, testBallotBox, numberOfWriteInFields, numberOfVotingCards,
				gracePeriod, primesMappingTable);
	}

	@Test
	@DisplayName("test ElectionEventContext constructor validation")
	void testInvalidElectionEventContext() {
		final String verificationCardSetId1 = random.genRandomBase16String(32).toLowerCase();
		final String verificationCardSetId2 = random.genRandomBase16String(32).toLowerCase();
		final String ballotBoxId1 = random.genRandomBase16String(32).toLowerCase();
		final String ballotBoxId2 = random.genRandomBase16String(32).toLowerCase();
		final String ballotBoxId4 = random.genRandomBase16String(32).toLowerCase();
		final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(
				List.of(new PrimesMappingTableEntry("actualVotingOption", smallPrimeGroupMembers.get(0))));

		final VerificationCardSetContext verificationCardSetContextOne = new VerificationCardSetContext(verificationCardSetId1, ballotBoxId1,
				false, 0, 10, 900, primesMappingTable);
		final VerificationCardSetContext verificationCardSetContextTwo = new VerificationCardSetContext(verificationCardSetId1, ballotBoxId2,
				true, 0, 10, 900, primesMappingTable);
		final VerificationCardSetContext verificationCardSetContextThree = new VerificationCardSetContext(verificationCardSetId2, ballotBoxId1,
				false, 2, 10, 900, primesMappingTable);
		final VerificationCardSetContext verificationCardSetContextFour = new VerificationCardSetContext(verificationCardSetId2, ballotBoxId4,
				true, 2, 10, 900, primesMappingTable);

		final String electionEventId = random.genRandomBase16String(32).toLowerCase();

		final List<VerificationCardSetContext> duplicateVerificationCardSetIds = new ArrayList<>();

		final List<ControlComponentPublicKeys> emptyCombinedControlComponentPublicKeys = new ArrayList<>();
		final ElGamalMultiRecipientPublicKey testElectoralBoardPublicKey = SerializationTestData.getPublicKey();
		final ElGamalMultiRecipientPublicKey testElectionPublicKey = SerializationTestData.getPublicKey();
		final ElGamalMultiRecipientPublicKey testChoiceReturnCodesPublicKey = SerializationTestData.getPublicKey();
		final LocalDateTime start = LocalDateTime.now();
		final LocalDateTime finish = start.plusWeeks(1);
		final IllegalArgumentException emptyIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateVerificationCardSetIds, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("VerificationCardSetContexts cannot be empty.", emptyIllegalArgumentException.getMessage());

		duplicateVerificationCardSetIds.add(verificationCardSetContextOne);
		duplicateVerificationCardSetIds.add(verificationCardSetContextTwo);

		final IllegalArgumentException duplicateVerificationCardSetIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateVerificationCardSetIds, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("VerificationCardSetContexts cannot contain duplicate VerificationCardSetIds.",
				duplicateVerificationCardSetIdsIllegalArgumentException.getMessage());

		final List<VerificationCardSetContext> duplicateBallotBoxIds = new ArrayList<>();
		duplicateBallotBoxIds.add(verificationCardSetContextOne);
		duplicateBallotBoxIds.add(verificationCardSetContextThree);

		final IllegalArgumentException duplicateBallotBoxIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateBallotBoxIds, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("VerificationCardSetContexts cannot contain duplicate BallotBoxIds.",
				duplicateBallotBoxIdsIllegalArgumentException.getMessage());

		final IllegalArgumentException negativeNumberOfWriteInFieldsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new VerificationCardSetContext(verificationCardSetId2, ballotBoxId4, true, -2, 10, 900, primesMappingTable));
		assertEquals("The number of write-in fields must be positive.",
				negativeNumberOfWriteInFieldsIllegalArgumentException.getMessage());

		final List<VerificationCardSetContext> correctVerificationCardSetContexts = new ArrayList<>();
		correctVerificationCardSetContexts.add(verificationCardSetContextOne);
		correctVerificationCardSetContexts.add(verificationCardSetContextFour);

		final List<ControlComponentPublicKeys> controlComponentPublicKeys = new ArrayList<>();
		final IllegalArgumentException emptyCombinedControlComponentPublicKeysIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, controlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("CombinedControlComponentPublicKeys must contain the expected number of ControlComponentPublicKeys.",
				emptyCombinedControlComponentPublicKeysIllegalArgumentException.getMessage());

		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(null, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, null, emptyCombinedControlComponentPublicKeys, testElectoralBoardPublicKey,
						schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, null, testElectoralBoardPublicKey, schnorrProofs,
						testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys, null,
						schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, null, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, null, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, null, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, schnorrProofs, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, null));
	}
}
