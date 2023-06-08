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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

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
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;

@DisplayName("A VerifiablePlaintextDecryption")
class VerifiablePlaintextDecryptionTest extends MapperSetUp {

	private static ObjectNode rootNode;

	private static GqGroup gqGroup;
	private static VerifiablePlaintextDecryption verifiablePlaintextDecryption;

	@BeforeAll
	static void setUpAll() {
		final int nbrMessage = 2;
		gqGroup = SerializationTestData.getGqGroup();

		final GroupVector<ElGamalMultiRecipientMessage, GqGroup> messages = SerializationTestData.getMessages(nbrMessage);
		final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = SerializationTestData.getDecryptionProofs(nbrMessage);
		verifiablePlaintextDecryption = new VerifiablePlaintextDecryption(messages, decryptionProofs);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final ArrayNode messagesNode = SerializationTestData.createMessagesNode(messages);
		rootNode.set("decryptedVotes", messagesNode);

		final ArrayNode decryptionProofsNode = SerializationTestData.createDecryptionProofsNode(decryptionProofs);
		rootNode.set("decryptionProofs", decryptionProofsNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeVerifiablePlaintextDecryption() throws JsonProcessingException {
		final String serializedVerifiablePlaintextDecryption = mapper.writeValueAsString(verifiablePlaintextDecryption);

		assertEquals(rootNode.toString(), serializedVerifiablePlaintextDecryption);
	}

	@Test
	@DisplayName("deserialized gives expected verifiablePlaintextDecryption")
	void deserializeVerificationPlaintextDecryption() throws IOException {
		final VerifiablePlaintextDecryption deserializedVerifiablePlaintextDecryption = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), VerifiablePlaintextDecryption.class);

		assertEquals(verifiablePlaintextDecryption, deserializedVerifiablePlaintextDecryption);
	}

	@Test
	@DisplayName("serialized then deserialized gives original verifiablePlaintextDecryption")
	void cycle() throws IOException {
		final VerifiablePlaintextDecryption result = mapper.reader().withAttribute("group", gqGroup)
				.readValue(mapper.writeValueAsString(verifiablePlaintextDecryption), VerifiablePlaintextDecryption.class);

		assertEquals(verifiablePlaintextDecryption, result);
	}

}
