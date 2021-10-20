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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@DisplayName("An ElGamalMultiRecipientMessage")
class ElGamalMultiRecipientMessageMixInTest extends MapperSetUp {

	private static ObjectNode rootNode;

	private static GqGroup gqGroup;
	private static ElGamalMultiRecipientMessage elGamalMultiRecipientMessage;

	@BeforeAll
	static void setUpAll() {
		gqGroup = SerializationUtils.getGqGroup();
		elGamalMultiRecipientMessage = SerializationUtils.getMessage();

		// Create expected json.
		rootNode = SerializationUtils.createMessageNode(elGamalMultiRecipientMessage);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeElGamalMultiRecipientMessage() throws JsonProcessingException {
		final String serializedMessage = mapper.writeValueAsString(elGamalMultiRecipientMessage);

		assertEquals(rootNode.toString(), serializedMessage);
	}

	@Test
	@DisplayName("deserialized gives expected message")
	void deserializeElGamalMultiRecipientMessage() throws IOException {
		final ElGamalMultiRecipientMessage deserializedMessage = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), ElGamalMultiRecipientMessage.class);

		assertEquals(elGamalMultiRecipientMessage, deserializedMessage);
	}

	@Test
	@DisplayName("serialized then deserialized gives original message")
	void cycle() throws IOException {
		final ElGamalMultiRecipientMessage result = mapper.reader().withAttribute("group", gqGroup)
				.readValue(mapper.writeValueAsString(elGamalMultiRecipientMessage), ElGamalMultiRecipientMessage.class);

		assertEquals(elGamalMultiRecipientMessage, result);
	}

}
