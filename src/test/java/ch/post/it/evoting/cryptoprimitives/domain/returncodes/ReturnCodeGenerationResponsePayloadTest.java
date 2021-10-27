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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;

@DisplayName("A ReturnCodeGenerationResponsePayload")
class ReturnCodeGenerationResponsePayloadTest extends MapperSetUp {

	private static final String TENANT_ID = "100";
	private static final String ELECTION_EVENT_ID = "1234";
	private static final String VERIFICATION_CARD_SET_ID = "1234";
	private static final int CHUNK_ID = 1;

	private static ReturnCodeGenerationResponsePayload responsePayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setUpAll() throws JsonProcessingException {
		responsePayload = SerializationTestData.getResponsePayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, CHUNK_ID);

		// Create expected json.
		rootNode = SerializationTestData.createResponsePayloadNode(responsePayload);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeReturnCodeGenerationResponsePayload() throws JsonProcessingException {
		final String serializedResponsePayload = mapper.writeValueAsString(responsePayload);

		assertEquals(rootNode.toString(), serializedResponsePayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializeReturnCodeGenerationResponsePayload() throws IOException {
		final ReturnCodeGenerationResponsePayload deserializedResponsePayload = mapper
				.readValue(rootNode.toString(), ReturnCodeGenerationResponsePayload.class);

		assertEquals(responsePayload, deserializedResponsePayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final ReturnCodeGenerationResponsePayload deserializedResponsePayload = mapper
				.readValue(mapper.writeValueAsString(responsePayload), ReturnCodeGenerationResponsePayload.class);

		assertEquals(responsePayload, deserializedResponsePayload);
	}

}