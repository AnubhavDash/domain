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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;

@DisplayName("A ControlComponentCodeShare")
class ControlComponentCodeShareTest extends MapperSetUp {

	private static final String VERIFICATION_CARD_ID = "cbf8ac1c1bcf444da0ccf5d7e956153b";

	private static ControlComponentCodeShare controlComponentCodeShare;
	private static ObjectNode rootNode;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		gqGroup = SerializationTestData.getGqGroup();

		final ElGamalMultiRecipientPublicKey voterChoiceReturnCodeGenerationPublicKey = SerializationTestData.getSingleElementPublicKey();
		final ElGamalMultiRecipientPublicKey voterVoteCastReturnCodeGenerationPublicKey = SerializationTestData.getSingleElementPublicKey();
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

	@Nested
	@DisplayName("contructed with")
	class ControlComponentCodeShareConsistencyCheckTest {

		private ElGamalMultiRecipientPublicKey voterChoiceReturnCodeGenerationPublicKey;
		private ElGamalMultiRecipientPublicKey voterVoteCastReturnCodeGenerationPublicKey;
		private ElGamalMultiRecipientCiphertext exponentiatedEncryptedPartialChoiceReturnCodes;
		private ExponentiationProof encryptedPartialChoiceReturnCodeExponentiationProof;
		private ElGamalMultiRecipientCiphertext exponentiatedEncryptedConfirmationKey;
		private ExponentiationProof encryptedConfirmationKeyExponentiationProof;

		@BeforeEach
		void setup() {
			voterChoiceReturnCodeGenerationPublicKey = SerializationTestData.getSingleElementPublicKey();
			voterVoteCastReturnCodeGenerationPublicKey = SerializationTestData.getSingleElementPublicKey();
			exponentiatedEncryptedPartialChoiceReturnCodes = SerializationTestData.getCiphertexts(1).get(0);
			encryptedPartialChoiceReturnCodeExponentiationProof = SerializationTestData.createExponentiationProof();
			exponentiatedEncryptedConfirmationKey = SerializationTestData.getSinglePhiCiphertext();
			encryptedConfirmationKeyExponentiationProof = SerializationTestData.createExponentiationProof();
		}

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void constructWithNullParameters() {
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeShare(null, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, null,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							null, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, null,
							encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							null, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, null,
							encryptedConfirmationKeyExponentiationProof));
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							null));
		}

		@Test
		@DisplayName("keys and choice return codes not having all the same group throws IllegalArgumentException")
		void constructWithInconsistentGroups() {
			final ElGamalMultiRecipientCiphertext otherExponentiatedEncryptedConfirmationKey = spy(exponentiatedEncryptedConfirmationKey);
			final GqGroup otherEncryptionGroup = GroupTestData.getDifferentGqGroup(SerializationTestData.getGqGroup());
			doReturn(otherEncryptionGroup).when(otherExponentiatedEncryptedConfirmationKey).getGroup();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, otherExponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertEquals("The keys and partial choice return codes must have the same group.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("Encrypted Partial Choice Return Code Exponentiation Proofs having different group order throws IllegalArgumentException")
		void constructWithEncryptedPartialChoiceReturnCodeExponentiationProofsDifferentGroupOrder() {
			final ExponentiationProof otherEncryptedPartialChoiceReturnCodeExponentiationProof = spy(encryptedPartialChoiceReturnCodeExponentiationProof);
			final ZqGroup otherGroup = GroupTestData.getDifferentZqGroup(ZqGroup.sameOrderAs(SerializationTestData.getGqGroup()));
			doReturn(otherGroup).when(otherEncryptedPartialChoiceReturnCodeExponentiationProof).getGroup();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							otherEncryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertEquals("The Encrypted Partial Choice Return Code Exponentiation Proofs must have the same group order as the keys.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("voter Choice Return Code Generation Public Key size greater 1 throws IllegalArgumentException")
		void constructWithTooLargeVoterChoiceReturnCodeGenerationPublicKey() {
			final ElGamalMultiRecipientPublicKey tooLargeVoterChoiceReturnCodeGenerationPublicKey = SerializationTestData.getPublicKey();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, tooLargeVoterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertEquals("The voter Choice Return Code Generation Public Key must be of size 1.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("voter Cast Code Generation Public Key size greater 1 throws IllegalArgumentException")
		void constructWithTooLargeVoterCastCodeGenerationPublicKey() {
			final ElGamalMultiRecipientPublicKey tooLargeVoterVoteCastCodeGenerationPublicKey = SerializationTestData.getPublicKey();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							tooLargeVoterVoteCastCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertEquals("The voter Vote Cast Return Code Generation Public Key must be of size 1.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("exponentiated Encrypted Confirmation Key size greater 1 throws IllegalArgumentException")
		void constructWithTooLargeExponentiatedEncryptedConfirmationKey() {
			final ElGamalMultiRecipientCiphertext tooLargeExponentiatedEncryptedConfirmationKey = SerializationTestData.getCiphertexts(1).get(0);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeShare(VERIFICATION_CARD_ID, voterChoiceReturnCodeGenerationPublicKey,
							voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
							encryptedPartialChoiceReturnCodeExponentiationProof, tooLargeExponentiatedEncryptedConfirmationKey,
							encryptedConfirmationKeyExponentiationProof));
			assertEquals("The exponentiated Encrypted Confirmation Key must be of size 1.", Throwables.getRootCause(exception).getMessage());
		}
	}
}