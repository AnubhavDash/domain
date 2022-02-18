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
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;

@DisplayName("A VerifiableShuffle")
class VerifiableShuffleTest extends MapperSetUp {

	private static final int NBR_CIPHERTEXT = 4;

	private static ObjectNode rootNode;
	private static VerifiableShuffle verifiableShuffle;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() throws JsonProcessingException {
		gqGroup = SerializationTestData.getGqGroup();

		final List<ElGamalMultiRecipientCiphertext> ciphertexts = SerializationTestData.getCiphertexts(NBR_CIPHERTEXT);
		final ShuffleArgument shuffleArgument = SerializationTestData.createShuffleArgument();

		verifiableShuffle = new VerifiableShuffle(GroupVector.from(ciphertexts), shuffleArgument);

		// Create expected json.
		rootNode = SerializationTestData.createVerifiableShuffleNode(verifiableShuffle);
	}

	@Test
	@DisplayName("serialized with mixIn gives expected json")
	void serializeVerifiableShuffleWithMixIn() throws JsonProcessingException {
		final String serializedShuffle = mapper.writeValueAsString(verifiableShuffle);

		assertEquals(rootNode.toString(), serializedShuffle);
	}

	@Test
	@DisplayName("deserialized with mixIn gives expected json")
	void deserializeVerifiableShuffleWithMixIn() throws IOException {
		final VerifiableShuffle deserializedVerifiableShuffle = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), VerifiableShuffle.class);

		assertEquals(verifiableShuffle, deserializedVerifiableShuffle);
	}

	@Test
	@DisplayName("serialized then deserialized with mixIn gives original VerifiableShuffle")
	void mixInCycle() throws IOException {
		final String serializedShuffle = mapper.writeValueAsString(verifiableShuffle);

		final VerifiableShuffle deserializedVerifiableShuffle = mapper.reader().withAttribute("group", gqGroup)
				.readValue(serializedShuffle, VerifiableShuffle.class);

		assertEquals(verifiableShuffle, deserializedVerifiableShuffle);
	}

}
