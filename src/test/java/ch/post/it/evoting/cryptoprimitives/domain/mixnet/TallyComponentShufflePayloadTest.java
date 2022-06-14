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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;

@DisplayName("A TallyComponentShufflePayload")
class TallyComponentShufflePayloadTest extends MapperSetUp {

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	private static ObjectNode rootNode;
	private static TallyComponentShufflePayload tallyComponentShufflePayload;

	@BeforeAll
	static void setUpAll() {
		final int nbrMessage = 4;
		final GqGroup gqGroup = SerializationTestData.getGqGroup();

		final VerifiableShuffle verifiableShuffle = SerializationTestData.getVerifiableShuffle(nbrMessage);
		final VerifiablePlaintextDecryption verifiablePlaintextDecryption = SerializationTestData.getVerifiablePlaintextDecryption(nbrMessage);

		// Generate random bytes for signature content and create payload signature.
		secureRandom.nextBytes(randomBytes);
		final X509Certificate certificate = SerializationTestData.generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });

		tallyComponentShufflePayload = new TallyComponentShufflePayload(gqGroup, verifiableShuffle, verifiablePlaintextDecryption, signature);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

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

}
