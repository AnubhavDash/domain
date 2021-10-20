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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@DisplayName("An ElGamalMultiRecipientCiphertext list")
class ElGamalMultiRecipientCiphertextTest extends MapperSetUp {

	private static ArrayNode rootNode;

	private static List<ElGamalMultiRecipientCiphertext> ciphertexts;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		ciphertexts = SerializationUtils.getCiphertexts(2);
		gqGroup = SerializationUtils.getGqGroup();
		rootNode = SerializationUtils.createCiphertextsNode(ciphertexts);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeElGamalMultiRecipientCiphertexts() throws JsonProcessingException {
		final String serializedCiphertexts = mapper.writeValueAsString(ciphertexts);

		assertEquals(rootNode.toString(), serializedCiphertexts);
	}

	@Test
	@DisplayName("deserialized gives expected ElGamalMultiRecipientCiphertext list")
	void deserializeElGamalMultiRecipientCiphertexts() throws IOException {
		final ElGamalMultiRecipientCiphertext[] deserializedCiphertexts = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), ElGamalMultiRecipientCiphertext[].class);

		assertEquals(ciphertexts, Arrays.asList(deserializedCiphertexts));
	}

	@Test
	@DisplayName("serialized then deserialized gives original ElGamalMultiRecipientCiphertext list")
	void deserializeElGamalMultiRecipientCiphertext() throws IOException {
		final ElGamalMultiRecipientCiphertext ciphertext = ciphertexts.get(0);

		final String serializedCiphertext = mapper.writeValueAsString(ciphertext);

		final ElGamalMultiRecipientCiphertext deserializedCiphertext = mapper.reader().withAttribute("group", gqGroup)
				.readValue(serializedCiphertext, ElGamalMultiRecipientCiphertext.class);

		assertEquals(ciphertext, deserializedCiphertext);
	}

}
