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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@DisplayName("An ElGamalMultiRecipientKeyPair")
class ElGamalMultiRecipientKeyPairMixInTest {

	private static final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	private static final RandomService randomService = new RandomService();
	private static final SecureRandom secureRandom = new SecureRandom();

	private static ObjectNode rootNode;
	private static ElGamalMultiRecipientKeyPair keyPair;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		gqGroup = GroupTestData.getGqGroup();

		final int numElements = secureRandom.nextInt(10) + 1;
		keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, numElements, randomService);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final ArrayNode publicKeyNode = SerializationTestData.createPublicKeyNode(keyPair.getPublicKey());
		rootNode.set("publicKey", publicKeyNode);

		final ArrayNode privateKeyNode = SerializationTestData.createPrivateKeyNode(keyPair.getPrivateKey());
		rootNode.set("privateKey", privateKeyNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeElGamalMultiRecipientKeyPair() throws JsonProcessingException {
		final String serializedMessage = mapper.writeValueAsString(keyPair);

		assertEquals(rootNode.toString(), serializedMessage);
	}

	@Test
	@DisplayName("deserialized gives expected key pair")
	void deserializeElGamalMultiRecipientKeyPair() throws IOException {
		final ElGamalMultiRecipientKeyPair deserializedKeyPair = mapper.reader()
				.withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), ElGamalMultiRecipientKeyPair.class);

		assertEquals(keyPair, deserializedKeyPair);
	}

	@Test
	@DisplayName("serialized then deserialized gives original key pair")
	void cycle() throws IOException {
		final ElGamalMultiRecipientKeyPair deserializedKeyPair = mapper.reader()
				.withAttribute("group", gqGroup)
				.readValue(mapper.writeValueAsString(keyPair), ElGamalMultiRecipientKeyPair.class);

		assertEquals(keyPair, deserializedKeyPair);
	}

}
