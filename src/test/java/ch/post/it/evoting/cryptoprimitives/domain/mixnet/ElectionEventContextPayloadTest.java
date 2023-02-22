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
	private static final GroupVector<SchnorrProof, ZqGroup> schnorrProofs = SerializationTestData.createSchnorrProofs(2);
	private static ElectionEventContextPayload electionEventContextPayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setupAll() throws JsonProcessingException {

		// Create payload.
		final String electionEventId = random.genRandomBase16String(32).toLowerCase();
		final List<VerificationCardSetContext> verificationCardSetContexts = new ArrayList<>();

		IntStream.rangeClosed(1, 2).forEach(i -> verificationCardSetContexts.add(generatedVerificationCardSetContext()));

		final LocalDateTime startTime = LocalDateTime.now();
		final LocalDateTime finishTime = startTime.plusWeeks(1);

		final ElectionEventContext electionEventContext = new ElectionEventContext(electionEventId, verificationCardSetContexts, startTime, finishTime);

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

		electionEventContextNode.set("startTime", mapper.readTree(mapper.writeValueAsString(startTime)));
		electionEventContextNode.set("finishTime", mapper.readTree(mapper.writeValueAsString(finishTime)));

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

		final LocalDateTime start = LocalDateTime.now();
		final LocalDateTime finish = start.plusWeeks(1);
		final IllegalArgumentException emptyIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateVerificationCardSetIds, start, finish));
		assertEquals("VerificationCardSetContexts cannot be empty.", emptyIllegalArgumentException.getMessage());

		duplicateVerificationCardSetIds.add(verificationCardSetContextOne);
		duplicateVerificationCardSetIds.add(verificationCardSetContextTwo);

		final IllegalArgumentException duplicateVerificationCardSetIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateVerificationCardSetIds, start, finish));
		assertEquals("VerificationCardSetContexts cannot contain duplicate VerificationCardSetIds.",
				duplicateVerificationCardSetIdsIllegalArgumentException.getMessage());

		final List<VerificationCardSetContext> duplicateBallotBoxIds = new ArrayList<>();
		duplicateBallotBoxIds.add(verificationCardSetContextOne);
		duplicateBallotBoxIds.add(verificationCardSetContextThree);

		final IllegalArgumentException duplicateBallotBoxIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateBallotBoxIds, start, finish));
		assertEquals("VerificationCardSetContexts cannot contain duplicate BallotBoxIds.",
				duplicateBallotBoxIdsIllegalArgumentException.getMessage());

		final IllegalArgumentException negativeNumberOfWriteInFieldsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new VerificationCardSetContext(verificationCardSetId2, ballotBoxId4, true, -2, 10, 900, primesMappingTable));
		assertEquals("The number of write-in fields must be positive.",
				negativeNumberOfWriteInFieldsIllegalArgumentException.getMessage());

		final List<VerificationCardSetContext> correctVerificationCardSetContexts = new ArrayList<>();
		correctVerificationCardSetContexts.add(verificationCardSetContextOne);
		correctVerificationCardSetContexts.add(verificationCardSetContextFour);

		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(null, correctVerificationCardSetContexts, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, null, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, null, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, start, null));
	}
}
