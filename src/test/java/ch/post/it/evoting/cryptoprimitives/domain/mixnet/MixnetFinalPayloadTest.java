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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;

@DisplayName("A MixnetFinalPayload")
class MixnetFinalPayloadTest extends MapperSetUp {

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	private static ObjectNode rootNode;
	private static GqGroup gqGroup;
	private static ElGamalMultiRecipientPublicKey previousRemainingPublicKey;
	private static VerifiablePlaintextDecryption verifiablePlaintextDecryption;
	private static CryptoPrimitivesPayloadSignature signature;
	private static MixnetFinalPayload mixnetFinalPayload;

	@BeforeAll
	static void setUpAll() {
		final int nbrMessage = 4;
		gqGroup = SerializationTestData.getGqGroup();

		final VerifiableShuffle verifiableShuffle = SerializationTestData.getVerifiableShuffle(nbrMessage);
		previousRemainingPublicKey = SerializationTestData.getPublicKey();
		verifiablePlaintextDecryption = SerializationTestData.getVerifiablePlaintextDecryption(nbrMessage);

		// Generate random bytes for signature content and create payload signature.
		secureRandom.nextBytes(randomBytes);
		final X509Certificate certificate = SerializationTestData.generateTestCertificate();
		signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });

		mixnetFinalPayload = new MixnetFinalPayload(gqGroup, verifiableShuffle, verifiablePlaintextDecryption, previousRemainingPublicKey, signature);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ObjectNode verifiableShuffleNode = SerializationTestData.createVerifiableShuffleNode(verifiableShuffle);
		rootNode.set("verifiableShuffle", verifiableShuffleNode);

		final ObjectNode verifiablePlaintextDecryptionNode = SerializationTestData
				.createVerifiablePlaintextDecryptionNode(verifiablePlaintextDecryption);
		rootNode.set("verifiablePlaintextDecryption", verifiablePlaintextDecryptionNode);

		final ArrayNode previousRemainingPublicKeyNode = SerializationTestData.createPublicKeyNode(previousRemainingPublicKey);
		rootNode.set("previousRemainingElectionPublicKey", previousRemainingPublicKeyNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeMixnetFinalPayload() throws JsonProcessingException {
		final String serializedMixnetFinalPayload = mapper.writeValueAsString(mixnetFinalPayload);

		assertEquals(rootNode.toString(), serializedMixnetFinalPayload);
	}

	@Test
	@DisplayName("deserialized gives expected MixnetFinalPayload")
	void deserializeMixnetFinalPayload() throws IOException {
		final MixnetFinalPayload deserializedMixnetFinalPayload = mapper.readValue(rootNode.toString(), MixnetFinalPayload.class);

		assertEquals(mixnetFinalPayload, deserializedMixnetFinalPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original MixnetFinalPayload")
	void cycle() throws IOException {
		final MixnetFinalPayload result = mapper.readValue(mapper.writeValueAsString(mixnetFinalPayload), MixnetFinalPayload.class);

		assertEquals(mixnetFinalPayload, result);
	}

	@Test
	@DisplayName("serialized then deserialized without VerifiableShuffle")
	void cycleWithoutVerifiableShuffle() throws IOException {
		final MixnetFinalPayload mixnetFinalPayload = new MixnetFinalPayload(gqGroup, null, verifiablePlaintextDecryption, previousRemainingPublicKey,
				signature);

		// Create expected json.
		final ObjectNode rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ObjectNode verifiablePlaintextDecryptionNode = SerializationTestData
				.createVerifiablePlaintextDecryptionNode(verifiablePlaintextDecryption);
		rootNode.set("verifiablePlaintextDecryption", verifiablePlaintextDecryptionNode);

		final ArrayNode previousRemainingPublicKeyNode = SerializationTestData.createPublicKeyNode(previousRemainingPublicKey);
		rootNode.set("previousRemainingElectionPublicKey", previousRemainingPublicKeyNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);

		final MixnetFinalPayload result = mapper.readValue(mapper.writeValueAsString(mixnetFinalPayload), MixnetFinalPayload.class);

		assertEquals(mixnetFinalPayload, result);
	}

}
