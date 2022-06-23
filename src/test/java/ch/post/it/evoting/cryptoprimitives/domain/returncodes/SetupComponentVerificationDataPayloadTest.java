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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@DisplayName("A SetupComponentVerificationDataPayload")
class SetupComponentVerificationDataPayloadTest extends MapperSetUp {

	private static final String TENANT_ID = "100";
	private static final String ELECTION_EVENT_ID = "4b7a8f063b564dbf8e24420d3f52f54f";
	private static final String VERIFICATION_CARD_SET_ID = "cbf8ac1c1bcf444da0ccf5d7e956153b";
	private static final int CHUNK_ID = 1;
	private static final String BALLOT_JSON = "ballot.json";

	private static SetupComponentVerificationDataPayload requestPayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setupAll() throws IOException {
		final Ballot ballot = getBallotFromResourceName();
		requestPayload = SerializationTestData.getRequestPayload(ballot, TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, CHUNK_ID);

		// Create expected json.
		rootNode = SerializationTestData.createRequestPayloadNode(requestPayload);
	}

	private static Ballot getBallotFromResourceName() throws IOException {
		return mapper.readValue(SetupComponentVerificationDataPayloadTest.class.getClassLoader().getResource(BALLOT_JSON), Ballot.class);
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
		final SetupComponentVerificationDataPayload deserializedPayload = mapper
				.readValue(rootNode.toString(), SetupComponentVerificationDataPayload.class);

		assertEquals(requestPayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final SetupComponentVerificationDataPayload deserializedPayload = mapper
				.readValue(mapper.writeValueAsString(requestPayload), SetupComponentVerificationDataPayload.class);

		assertEquals(requestPayload, deserializedPayload);
	}

	@Nested
	@DisplayName("constructed with")
	class SetupComponentVerificationDataPayloadConsistencyCheckTest {

		private List<String> partialChoiceReturnCodesAllowList;
		private GqGroup encryptionGroup;
		private List<SetupComponentVerificationData> setupComponentVerificationData;
		private CombinedCorrectnessInformation combinedCorrectnessInformation;

		@BeforeEach
		void setup() {
			partialChoiceReturnCodesAllowList = requestPayload.getPartialChoiceReturnCodesAllowList();
			encryptionGroup = requestPayload.getEncryptionGroup();
			setupComponentVerificationData = requestPayload.getSetupComponentVerificationData();
			combinedCorrectnessInformation = requestPayload.getCombinedCorrectnessInformation();
		}

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void constructWithNullParameters() {
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationDataPayload(null, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID,
							partialChoiceReturnCodesAllowList, CHUNK_ID, encryptionGroup, setupComponentVerificationData,
							combinedCorrectnessInformation));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, null, VERIFICATION_CARD_SET_ID, partialChoiceReturnCodesAllowList,
							CHUNK_ID, encryptionGroup, setupComponentVerificationData, combinedCorrectnessInformation));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, ELECTION_EVENT_ID, null, partialChoiceReturnCodesAllowList, CHUNK_ID,
							encryptionGroup, setupComponentVerificationData, combinedCorrectnessInformation));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, null, CHUNK_ID,
							encryptionGroup, setupComponentVerificationData, combinedCorrectnessInformation));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID,
							partialChoiceReturnCodesAllowList, CHUNK_ID, null, setupComponentVerificationData, combinedCorrectnessInformation));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID,
							partialChoiceReturnCodesAllowList, CHUNK_ID, encryptionGroup, null, combinedCorrectnessInformation));
			assertThrows(NullPointerException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID,
							partialChoiceReturnCodesAllowList, CHUNK_ID, encryptionGroup, setupComponentVerificationData, null));
		}

		@Test
		@DisplayName("empty partialChoiceReturnCodesAllowList throws IllegalArgumentException")
		void constructWithEmptyPartialChoiceReturnCodesAllowList() {
			final List<String> emptyPartialChoiceReturnCodesAllowList = List.of();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID,
							emptyPartialChoiceReturnCodesAllowList, CHUNK_ID, encryptionGroup, setupComponentVerificationData,
							combinedCorrectnessInformation));
			assertEquals("The partial Choice Return Codes Allow List must not be empty.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("partialChoiceReturnCodesAllowList containing null elements throws IllegalArgumentException")
		void constructWithPartialChoiceReturnCodesAllowListContainingNull() {
			final List<String> partialChoiceReturnCodesAllowListWithNull = Collections.singletonList(null);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new SetupComponentVerificationDataPayload(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID,
							partialChoiceReturnCodesAllowListWithNull, CHUNK_ID, encryptionGroup, setupComponentVerificationData,
							combinedCorrectnessInformation));
			assertEquals("The partial Choice Return Codes Allow List must not contain null elements.", Throwables.getRootCause(exception).getMessage());
		}
	}
}
