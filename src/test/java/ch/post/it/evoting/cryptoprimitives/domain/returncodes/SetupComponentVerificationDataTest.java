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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@DisplayName("A SetupComponentVerificationData")
class SetupComponentVerificationDataTest extends MapperSetUp {

	private static final String VERIFICATION_CARD_ID = "1234";

	private static SetupComponentVerificationData setupComponentVerificationData;
	private static ObjectNode rootNode;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		gqGroup = SerializationTestData.getGqGroup();

		final ElGamalMultiRecipientCiphertext encryptedHashedSquaredConfirmationKey = SerializationTestData.getSinglePhiCiphertext();
		final ElGamalMultiRecipientCiphertext encryptedHashedSquaredPartialChoiceReturnCodes = SerializationTestData.getCiphertexts(1).get(0);
		final ElGamalMultiRecipientPublicKey verificationCardPublicKey = SerializationTestData.getPublicKey();

		setupComponentVerificationData = new SetupComponentVerificationData(VERIFICATION_CARD_ID, encryptedHashedSquaredConfirmationKey,
				encryptedHashedSquaredPartialChoiceReturnCodes, verificationCardPublicKey);

		// Create expected json.
		rootNode = mapper.createObjectNode();
		rootNode.put("verificationCardId", VERIFICATION_CARD_ID);

		final ObjectNode confirmationKeyNode = SerializationTestData.createCiphertextNode(encryptedHashedSquaredConfirmationKey);
		rootNode.set("encryptedHashedSquaredConfirmationKey", confirmationKeyNode);

		final ObjectNode partialChoiceCodesNode = SerializationTestData.createCiphertextNode(encryptedHashedSquaredPartialChoiceReturnCodes);
		rootNode.set("encryptedHashedSquaredPartialChoiceReturnCodes", partialChoiceCodesNode);

		final ArrayNode verificationCardPublicKeyNode = SerializationTestData.createPublicKeyNode(verificationCardPublicKey);
		rootNode.set("verificationCardPublicKey", verificationCardPublicKeyNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeReturnCodeGenerationInput() throws JsonProcessingException {
		final String serializedInput = mapper.writeValueAsString(setupComponentVerificationData);

		assertEquals(rootNode.toString(), serializedInput);
	}

	@Test
	@DisplayName("deserialized gives expected input")
	void deserializeReturnCodeGenerationInput() throws IOException {
		final SetupComponentVerificationData deserializedInput = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), SetupComponentVerificationData.class);

		assertEquals(setupComponentVerificationData, deserializedInput);
	}

	@Test
	@DisplayName("serialized then deserialized gives original input")
	void cycle() throws IOException {
		final SetupComponentVerificationData deserializedInput = mapper.reader().withAttribute("group", gqGroup)
				.readValue(mapper.writeValueAsString(setupComponentVerificationData), SetupComponentVerificationData.class);

		assertEquals(setupComponentVerificationData, deserializedInput);
	}

}
