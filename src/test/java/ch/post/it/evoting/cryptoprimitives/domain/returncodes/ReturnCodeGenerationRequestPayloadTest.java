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
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;

@DisplayName("A ReturnCodeGenerationRequestPayload")
class ReturnCodeGenerationRequestPayloadTest extends MapperSetUp {

	private static final String TENANT_ID = "100";
	private static final String ELECTION_EVENT_ID = "1234";
	private static final String VERIFICATION_CARD_SET_ID = "1234";
	private static final int CHUNK_ID = 1;
	private static final String BALLOT_JSON = "ballot.json";

	private static ReturnCodeGenerationRequestPayload requestPayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setupAll() throws IOException {
		final Ballot ballot = getBallotFromResourceName();
		requestPayload = SerializationTestData.getRequestPayload(ballot, TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, CHUNK_ID);

		// Create expected json.
		rootNode = SerializationTestData.createRequestPayloadNode(requestPayload);
	}

	private static Ballot getBallotFromResourceName() throws IOException {
		return mapper.readValue(ReturnCodeGenerationRequestPayloadTest.class.getClassLoader().getResource(BALLOT_JSON), Ballot.class);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeReturnCodeGenerationRequestPayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(requestPayload);

		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializeReturnCodeGenerationRequestPayload() throws IOException {
		final ReturnCodeGenerationRequestPayload deserializedPayload = mapper
				.readValue(rootNode.toString(), ReturnCodeGenerationRequestPayload.class);

		assertEquals(requestPayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final ReturnCodeGenerationRequestPayload deserializedPayload = mapper
				.readValue(mapper.writeValueAsString(requestPayload), ReturnCodeGenerationRequestPayload.class);

		assertEquals(requestPayload, deserializedPayload);
	}

}
