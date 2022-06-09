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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;

@DisplayName("A ChoiceCodeGenerationDTO")
class ChoiceCodeGenerationDTOTest extends MapperSetUp {

	private static final String TENANT_ID = "100";
	private static final String ELECTION_EVENT_ID = "1234";
	private static final String VERIFICATION_CARD_SET_ID = "5678";
	private static final int CHUNK_ID = 1;
	private static final String BALLOT_JSON = "ballot.json";

	private static Ballot getBallotFromResourceName() throws IOException {
		return mapper.readValue(ChoiceCodeGenerationDTOTest.class.getClassLoader().getResource(BALLOT_JSON), Ballot.class);
	}

	@Nested
	@DisplayName("with a request payload")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class WithRequestPayload {

		private ChoiceCodeGenerationDTO<SetupComponentVerificationDataPayload> choiceCodeGenerationDTO;
		private ObjectNode rootNode;

		@BeforeAll
		void setupAll() throws IOException {
			final Ballot ballot = getBallotFromResourceName();
			final SetupComponentVerificationDataPayload requestPayload = SerializationTestData
					.getRequestPayload(ballot, TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, CHUNK_ID);

			final UUID randomUUID = UUID.randomUUID();
			final String requestId = "5555";
			choiceCodeGenerationDTO = new ChoiceCodeGenerationDTO<>(randomUUID, requestId, requestPayload);

			// Create expected json.
			rootNode = mapper.createObjectNode();
			rootNode.put("correlationId", randomUUID.toString());
			rootNode.put("requestId", requestId);

			final ObjectNode requestPayloadNode = SerializationTestData.createRequestPayloadNode(requestPayload);
			rootNode.set("payload", requestPayloadNode);
		}

		@Test
		@DisplayName("serialized gives expected json")
		void serializeDTOWithRequestPayload() throws JsonProcessingException {
			final String serializedDTO = mapper.writeValueAsString(choiceCodeGenerationDTO);

			assertEquals(rootNode.toString(), serializedDTO);
		}

		@Test
		@DisplayName("deserialized gives expected dto")
		void deserializeDTOWithRequestPayload() throws JsonProcessingException {
			final ChoiceCodeGenerationDTO<SetupComponentVerificationDataPayload> deserializedDTO = mapper
					.readValue(rootNode.toString(), new TypeReference<ChoiceCodeGenerationDTO<SetupComponentVerificationDataPayload>>() {
					});

			assertEquals(choiceCodeGenerationDTO, deserializedDTO);
		}

		@Test
		@DisplayName("serialized then deserialized gives original dto")
		void cycle() throws JsonProcessingException {
			final ChoiceCodeGenerationDTO<SetupComponentVerificationDataPayload> deserializedDTO = mapper
					.readValue(mapper.writeValueAsString(choiceCodeGenerationDTO),
							new TypeReference<ChoiceCodeGenerationDTO<SetupComponentVerificationDataPayload>>() {
							});

			assertEquals(choiceCodeGenerationDTO, deserializedDTO);
		}
	}

	@Nested
	@DisplayName("with a response payload")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class WithResponsePayload {

		private ChoiceCodeGenerationDTO<ControlComponentCodeSharesPayload> choiceCodeGenerationDTO;
		private ObjectNode rootNode;

		@BeforeAll
		void setupAll() throws IOException {
			final ControlComponentCodeSharesPayload responsePayload = SerializationTestData
					.getResponsePayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, CHUNK_ID);

			final UUID randomUUID = UUID.randomUUID();
			final String requestId = "5555";
			choiceCodeGenerationDTO = new ChoiceCodeGenerationDTO<>(randomUUID, requestId, responsePayload);

			// Create expected json.
			rootNode = mapper.createObjectNode();
			rootNode.put("correlationId", randomUUID.toString());
			rootNode.put("requestId", requestId);

			final ObjectNode requestPayloadNode = SerializationTestData.createResponsePayloadNode(responsePayload);
			rootNode.set("payload", requestPayloadNode);
		}

		@Test
		@DisplayName("serialized gives expected json")
		void serializeDTOWithRequestPayload() throws JsonProcessingException {
			final String serializedDTO = mapper.writeValueAsString(choiceCodeGenerationDTO);

			assertEquals(rootNode.toString(), serializedDTO);
		}

		@Test
		@DisplayName("deserialized gives expected dto")
		void deserializeDTOWithRequestPayload() throws JsonProcessingException {
			final ChoiceCodeGenerationDTO<ControlComponentCodeSharesPayload> deserializedDTO = mapper
					.readValue(rootNode.toString(), new TypeReference<ChoiceCodeGenerationDTO<ControlComponentCodeSharesPayload>>() {
					});

			assertEquals(choiceCodeGenerationDTO, deserializedDTO);
		}

		@Test
		@DisplayName("serialized then deserialized gives original dto")
		void cycle() throws JsonProcessingException {
			final ChoiceCodeGenerationDTO<ControlComponentCodeSharesPayload> deserializedDTO = mapper
					.readValue(mapper.writeValueAsString(choiceCodeGenerationDTO),
							new TypeReference<ChoiceCodeGenerationDTO<ControlComponentCodeSharesPayload>>() {
							});

			assertEquals(choiceCodeGenerationDTO, deserializedDTO);
		}

	}

}