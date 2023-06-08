/*
 * Copyright 2023 Post CH Ltd
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@DisplayName("A SetupComponentVerificationData")
class SetupComponentVerificationDataTest extends MapperSetUp {

	private static final String VERIFICATION_CARD_ID = "4b7a8f063b564dbf8e24420d3f52f54f";

	private static SetupComponentVerificationData setupComponentVerificationData;
	private static ObjectNode rootNode;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		gqGroup = SerializationTestData.getGqGroup();

		final ElGamalMultiRecipientCiphertext encryptedHashedSquaredConfirmationKey = SerializationTestData.getSinglePhiCiphertext();
		final ElGamalMultiRecipientCiphertext encryptedHashedSquaredPartialChoiceReturnCodes = SerializationTestData.getCiphertexts(1).get(0);
		final ElGamalMultiRecipientPublicKey verificationCardPublicKey = SerializationTestData.getSingleElementPublicKey();

		setupComponentVerificationData = new SetupComponentVerificationData(VERIFICATION_CARD_ID, encryptedHashedSquaredConfirmationKey,
				encryptedHashedSquaredPartialChoiceReturnCodes, verificationCardPublicKey);

		// Create expected json.
		rootNode = mapper.createObjectNode();
		rootNode.put("verificationCardId", VERIFICATION_CARD_ID);

		final ObjectNode confirmationKeyNode = SerializationTestData.createCiphertextNodeBase64(encryptedHashedSquaredConfirmationKey);
		rootNode.set("encryptedHashedSquaredConfirmationKey", confirmationKeyNode);

		final ObjectNode partialChoiceCodesNode = SerializationTestData.createCiphertextNodeBase64(encryptedHashedSquaredPartialChoiceReturnCodes);
		rootNode.set("encryptedHashedSquaredPartialChoiceReturnCodes", partialChoiceCodesNode);

		final ArrayNode verificationCardPublicKeyNode = SerializationTestData.createPublicKeyNodeBase64(verificationCardPublicKey);
		rootNode.set("verificationCardPublicKey", verificationCardPublicKeyNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeReturnCodeGenerationInput() throws JsonProcessingException {
		final String serializedInput = mapper
				.writer().withAttribute("base64Conversion", true)
				.writeValueAsString(setupComponentVerificationData);

		assertEquals(rootNode.toString(), serializedInput);
	}

	@Test
	@DisplayName("deserialized gives expected input")
	void deserializeReturnCodeGenerationInput() throws IOException {
		final SetupComponentVerificationData deserializedInput = mapper.reader()
				.withAttribute("group", gqGroup)
				.withAttribute("base64Conversion", true)
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

	@Nested
	@DisplayName("constructed with")
	class SetupComponentVerificationDataConsistencyCheckTest {

		private ElGamalMultiRecipientCiphertext encryptedHashedSquaredConfirmationKey;
		private ElGamalMultiRecipientCiphertext encryptedHashedSquaredPartialChoiceReturnCodes;
		private ElGamalMultiRecipientPublicKey verificationCardPublicKey;

		@BeforeEach
		void setup() {
			encryptedHashedSquaredConfirmationKey = SerializationTestData.getSinglePhiCiphertext();
			encryptedHashedSquaredPartialChoiceReturnCodes = SerializationTestData.getCiphertexts(1).get(0);
			verificationCardPublicKey = SerializationTestData.getSingleElementPublicKey();
		}

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void constructWithNullParameters() {
			assertThrows(NullPointerException.class, () -> new SetupComponentVerificationData(null, encryptedHashedSquaredConfirmationKey,
					encryptedHashedSquaredPartialChoiceReturnCodes, verificationCardPublicKey));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationData(VERIFICATION_CARD_ID, null, encryptedHashedSquaredPartialChoiceReturnCodes,
							verificationCardPublicKey));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationData(VERIFICATION_CARD_ID, encryptedHashedSquaredConfirmationKey, null,
							verificationCardPublicKey));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationData(VERIFICATION_CARD_ID, encryptedHashedSquaredConfirmationKey,
							encryptedHashedSquaredPartialChoiceReturnCodes, null));
		}

		@Test
		@DisplayName("confirmation key having different group than partial choice return codes throws IllegalArgumentException")
		void constructWithConfirmationKeyPartialChoiceReturnCodesDifferentGroup() {
			final ElGamalMultiRecipientCiphertext otherEncryptedHashedSquaredPartialChoiceReturnCodes = spy(encryptedHashedSquaredPartialChoiceReturnCodes);
			final GqGroup otherEncryptionGroup = GroupTestData.getDifferentGqGroup(SerializationTestData.getGqGroup());
			doReturn(otherEncryptionGroup).when(otherEncryptedHashedSquaredPartialChoiceReturnCodes).getGroup();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new SetupComponentVerificationData(VERIFICATION_CARD_ID, encryptedHashedSquaredConfirmationKey,
							otherEncryptedHashedSquaredPartialChoiceReturnCodes, verificationCardPublicKey));
			assertEquals("The encrypted hashed squared confirmation key and the encrypted hashed squared Partial Choice Return Codes must have the same group.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("verification card public key having different group than confirmation key throws IllegalArgumentException")
		void constructWithVerificationCardPublicKeyConfirmationKeyDifferentGroup() {
			final ElGamalMultiRecipientPublicKey otherVerificationCardPublicKey = spy(verificationCardPublicKey);
			final GqGroup otherEncryptionGroup = GroupTestData.getDifferentGqGroup(SerializationTestData.getGqGroup());
			doReturn(otherEncryptionGroup).when(otherVerificationCardPublicKey).getGroup();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new SetupComponentVerificationData(VERIFICATION_CARD_ID, encryptedHashedSquaredConfirmationKey,
							encryptedHashedSquaredPartialChoiceReturnCodes, otherVerificationCardPublicKey));
			assertEquals("The encrypted hashed squared confirmation key and the verification card public key must have the same group.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("encrypted hashed squared confirmation key with more than 1 element throws IllegalArgumentException")
		void constructWithTooLargeEncryptedHashedSquaredConfirmationKey() {
			final ElGamalMultiRecipientCiphertext tooLargeEncryptedHashedSquaredConfirmationKey = SerializationTestData.getCiphertexts(1).get(0);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new SetupComponentVerificationData(VERIFICATION_CARD_ID, tooLargeEncryptedHashedSquaredConfirmationKey,
							encryptedHashedSquaredPartialChoiceReturnCodes, verificationCardPublicKey));
			assertEquals("The encrypted hashed squared confirmation key must be of size 1.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("verification card public key with more than 1 element throws IllegalArgumentException")
		void constructWithTooLargeVerificationCardPublicKey() {
			final ElGamalMultiRecipientPublicKey tooLargeVerificationCardPublicKey = SerializationTestData.getPublicKey();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new SetupComponentVerificationData(VERIFICATION_CARD_ID, encryptedHashedSquaredConfirmationKey,
							encryptedHashedSquaredPartialChoiceReturnCodes, tooLargeVerificationCardPublicKey));
			assertEquals("The verification card public key must be of size 1.", Throwables.getRootCause(exception).getMessage());
		}
	}
}
