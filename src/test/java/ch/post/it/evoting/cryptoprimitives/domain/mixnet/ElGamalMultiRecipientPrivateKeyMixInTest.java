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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@DisplayName("An ElGamalMultiRecipientPrivateKey")
class ElGamalMultiRecipientPrivateKeyMixInTest extends MapperSetUp {

	private static ArrayNode rootNode;

	private static GqGroup gqGroup;
	private static ElGamalMultiRecipientPrivateKey elGamalMultiRecipientPrivateKey;

	@BeforeAll
	static void setUpAll() {
		gqGroup = SerializationTestData.getGqGroup();
		elGamalMultiRecipientPrivateKey = SerializationTestData.getPrivateKey();

		// Create expected json.
		rootNode = SerializationTestData.createPrivateKeyNode(elGamalMultiRecipientPrivateKey);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeElGamalMultiRecipientPrivateKey() throws JsonProcessingException {
		final String serializedMessage = mapper.writeValueAsString(elGamalMultiRecipientPrivateKey);

		assertEquals(rootNode.toString(), serializedMessage);
	}

	@Test
	@DisplayName("deserialized gives expected private key")
	void deserializeElGamalMultiRecipientPrivateKey() throws IOException {
		final ElGamalMultiRecipientPrivateKey deserializedPrivateKey = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), ElGamalMultiRecipientPrivateKey.class);

		assertEquals(elGamalMultiRecipientPrivateKey, deserializedPrivateKey);
	}

	@Test
	@DisplayName("serialized then deserialized gives original private key")
	void cycle() throws IOException {
		final ElGamalMultiRecipientPrivateKey deserializedPrivateKey = mapper.reader().withAttribute("group", gqGroup)
				.readValue(mapper.writeValueAsString(elGamalMultiRecipientPrivateKey), ElGamalMultiRecipientPrivateKey.class);

		assertEquals(elGamalMultiRecipientPrivateKey, deserializedPrivateKey);
	}

}