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
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;

@DisplayName("A ControlComponentCodeShare")
class ControlComponentCodeShareTest extends MapperSetUp {

	private static final String VERIFICATION_CARD_ID = "1234";

	private static ControlComponentCodeShare controlComponentCodeShare;
	private static ObjectNode rootNode;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		gqGroup = SerializationTestData.getGqGroup();

		final ElGamalMultiRecipientPublicKey voterChoiceReturnCodeGenerationPublicKey = SerializationTestData.getPublicKey();
		final ElGamalMultiRecipientPublicKey voterVoteCastReturnCodeGenerationPublicKey = SerializationTestData.getPublicKey();
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedPartialChoiceReturnCodes = SerializationTestData.getCiphertexts(1).get(0);
		final ExponentiationProof encryptedPartialChoiceReturnCodeExponentiationProof = SerializationTestData.createExponentiationProof();
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedConfirmationKey = SerializationTestData.getSinglePhiCiphertext();
		final ExponentiationProof encryptedConfirmationKeyExponentiationProof = SerializationTestData.createExponentiationProof();

		controlComponentCodeShare = new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
				voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
				encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
				encryptedConfirmationKeyExponentiationProof);

		// Creat expected json.
		rootNode = mapper.createObjectNode();
		rootNode.put("verificationCardId", VERIFICATION_CARD_ID);

		final ArrayNode voterChoicePublicKey = SerializationTestData.createPublicKeyNode(voterChoiceReturnCodeGenerationPublicKey);
		rootNode.set("voterChoiceReturnCodeGenerationPublicKey", voterChoicePublicKey);

		final ArrayNode voterVoteCastPublicKey = SerializationTestData.createPublicKeyNode(voterVoteCastReturnCodeGenerationPublicKey);
		rootNode.set("voterVoteCastReturnCodeGenerationPublicKey", voterVoteCastPublicKey);

		final ObjectNode partialChoiceCodeNode = SerializationTestData.createCiphertextNode(exponentiatedEncryptedPartialChoiceReturnCodes);
		rootNode.set("exponentiatedEncryptedPartialChoiceReturnCodes", partialChoiceCodeNode);

		final ObjectNode partialChoiceCodeProofNode = SerializationTestData
				.createExponentiationProofNode(encryptedPartialChoiceReturnCodeExponentiationProof);
		rootNode.set("encryptedPartialChoiceReturnCodeExponentiationProof", partialChoiceCodeProofNode);

		final ObjectNode confirmationKeyNode = SerializationTestData.createCiphertextNode(exponentiatedEncryptedConfirmationKey);
		rootNode.set("exponentiatedEncryptedConfirmationKey", confirmationKeyNode);

		final ObjectNode confirmationKeyProofNode = SerializationTestData.createExponentiationProofNode(encryptedConfirmationKeyExponentiationProof);
		rootNode.set("encryptedConfirmationKeyExponentiationProof", confirmationKeyProofNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeReturnCodeGenerationOutput() throws JsonProcessingException {
		final String serializedOutput = mapper.writeValueAsString(controlComponentCodeShare);

		assertEquals(rootNode.toString(), serializedOutput);
	}

	@Test
	@DisplayName("deserialized gives expected output")
	void deserializeReturnCodeGenerationOutput() throws IOException {
		final ControlComponentCodeShare deserializedOutput = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), ControlComponentCodeShare.class);

		assertEquals(controlComponentCodeShare, deserializedOutput);
	}

	@Test
	@DisplayName("serialized then deserialized gives original output")
	void cycle() throws IOException {
		final ControlComponentCodeShare deserializedOutput = mapper.reader().withAttribute("group", gqGroup)
				.readValue(mapper.writeValueAsString(controlComponentCodeShare), ControlComponentCodeShare.class);

		assertEquals(controlComponentCodeShare, deserializedOutput);
	}

}