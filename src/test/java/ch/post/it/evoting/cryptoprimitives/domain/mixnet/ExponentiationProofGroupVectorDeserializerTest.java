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
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;

@DisplayName("An object with a GroupVector of exponentiation proofs")
class ExponentiationProofGroupVectorDeserializerTest extends MapperSetUp {

	private static ObjectWithExponentiationProofs objectWithExponentiationProofs;
	private static ObjectNode rootNode;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		gqGroup = GroupTestData.getGqGroup();
		final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(ZqGroup.sameOrderAs(gqGroup));

		final ZqElement e = zqGroupGenerator.genRandomZqElementMember();
		final ZqElement z = zqGroupGenerator.genRandomZqElementMember();
		final ExponentiationProof exponentiationProof = new ExponentiationProof(e, z);
		final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs = GroupVector.of(exponentiationProof, exponentiationProof);

		objectWithExponentiationProofs = new ObjectWithExponentiationProofs(exponentiationProofs);

		// Create expected Json.
		rootNode = mapper.createObjectNode();

		final ArrayNode exponentiationProofsNode = SerializationTestData.createExponentiationProofsNode(exponentiationProofs);
		rootNode.set("exponentiationProofs", exponentiationProofsNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(objectWithExponentiationProofs);

		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected object")
	void deserializePayload() throws IOException {
		final ObjectWithExponentiationProofs deserializedPayload = mapper.reader()
				.withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), ObjectWithExponentiationProofs.class);

		assertEquals(objectWithExponentiationProofs, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original object")
	void cycle() throws IOException {
		final ObjectWithExponentiationProofs deserializedObject = mapper.reader()
				.withAttribute("group", gqGroup)
				.readValue(mapper.writeValueAsString(objectWithExponentiationProofs), ObjectWithExponentiationProofs.class);

		assertEquals(objectWithExponentiationProofs, deserializedObject);
	}

	@JsonPropertyOrder({ "exponentiationProofs" })
	private static class ObjectWithExponentiationProofs {

		@JsonProperty
		@JsonDeserialize(using = ExponentiationProofGroupVectorDeserializer.class)
		private final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs;

		@JsonCreator
		ObjectWithExponentiationProofs(
				@JsonProperty("exponentiationProofs")
				final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs) {
			this.exponentiationProofs = Preconditions.checkNotNull(exponentiationProofs);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final ObjectWithExponentiationProofs that = (ObjectWithExponentiationProofs) o;
			return exponentiationProofs.equals(that.exponentiationProofs);
		}

		@Override
		public int hashCode() {
			return Objects.hash(exponentiationProofs);
		}
	}

}
