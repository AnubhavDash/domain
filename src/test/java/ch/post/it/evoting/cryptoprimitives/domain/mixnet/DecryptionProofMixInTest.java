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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.GroupVector;
import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;

@DisplayName("A DecryptionProof")
class DecryptionProofMixInTest extends MapperSetUp {

	private static DecryptionProof decryptionProof;
	private static ObjectNode rootNode;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() throws JsonProcessingException {
		gqGroup = SerializationTestData.getGqGroup();
		final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);

		final ZqElement e = ZqElement.create(2, zqGroup);
		final GroupVector<ZqElement, ZqGroup> z = GroupVector.of(ZqElement.create(1, zqGroup), ZqElement.create(3, zqGroup));

		decryptionProof = new DecryptionProof(e, z);

		// Create expected json.
		rootNode = mapper.createObjectNode();

		final JsonNode eNode = mapper.readTree(mapper.writeValueAsString(e));
		rootNode.set("e", eNode);

		final JsonNode zNode = mapper.readTree(mapper.writeValueAsString(z));
		rootNode.set("z", zNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeDecryptionProof() throws JsonProcessingException {
		final String serializedDecryptionProof = mapper.writeValueAsString(decryptionProof);

		assertEquals(rootNode.toString(), serializedDecryptionProof);
	}

	@Test
	@DisplayName("deserialized gives expected DecryptionProof")
	void deserializeDecryptionProof() throws IOException {
		final DecryptionProof deserializedDecryptionProof = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), DecryptionProof.class);

		assertEquals(decryptionProof, deserializedDecryptionProof);
	}

	@Test
	@DisplayName("serialized then deserialized gives original DecryptionProof")
	void cycle() throws IOException {
		final String serializedDecryptionProof = mapper.writeValueAsString(decryptionProof);

		final DecryptionProof deserializedDecryptionProof = mapper.reader().withAttribute("group", gqGroup)
				.readValue(serializedDecryptionProof, DecryptionProof.class);

		assertEquals(decryptionProof, deserializedDecryptionProof);
	}

}