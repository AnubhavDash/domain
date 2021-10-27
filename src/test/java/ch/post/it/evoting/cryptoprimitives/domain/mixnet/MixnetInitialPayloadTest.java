/*
 * Copyright 2021 Post CH Ltd
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
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@DisplayName("A MixnetInitialPayload")
class MixnetInitialPayloadTest extends MapperSetUp {

	private static final int NBR_CIPHERTEXT = 10;
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	private static MixnetPayload initialPayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setUpAll() throws IOException {
		final GqGroup gqGroup = SerializationTestData.getGqGroup();

		final List<ElGamalMultiRecipientCiphertext> ciphertexts = SerializationTestData.getCiphertexts(NBR_CIPHERTEXT);
		final ElGamalMultiRecipientPublicKey electionPublicKey = SerializationTestData.getPublicKey();

		// Generate random bytes for signature content and create payload signature.
		secureRandom.nextBytes(randomBytes);
		final X509Certificate certificate = SerializationTestData.generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });

		initialPayload = new MixnetInitialPayload(gqGroup, ciphertexts, electionPublicKey, signature);

		// Create expected Json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = mapper.readTree(mapper.writeValueAsString(gqGroup));
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ArrayNode ciphertextsNode = SerializationTestData.createCiphertextsNode(ciphertexts);
		rootNode.set("ciphertexts", ciphertextsNode);

		final ArrayNode electionPublicKeyNode = SerializationTestData.createPublicKeyNode(electionPublicKey);
		rootNode.set("electionPublicKey", electionPublicKeyNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeMixnetInitialPayload() throws JsonProcessingException {
		final String serializedInitialPayload = mapper.writeValueAsString(initialPayload);

		assertEquals(rootNode.toString(), serializedInitialPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializeMixnetInitialPayload() throws IOException {
		final MixnetInitialPayload deserializedRequest = mapper.readValue(rootNode.toString(), MixnetInitialPayload.class);

		assertEquals(initialPayload, deserializedRequest);
	}

	@Test
	@DisplayName("serialized and deserialized gives original payload")
	void cycle() throws IOException {
		final MixnetInitialPayload result = mapper.readValue(mapper.writeValueAsString(initialPayload), MixnetInitialPayload.class);

		assertEquals(initialPayload, result);
	}

}