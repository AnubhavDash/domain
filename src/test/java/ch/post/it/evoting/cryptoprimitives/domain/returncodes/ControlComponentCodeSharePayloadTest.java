/*
 * Copyright 2023 Post CH Ltd
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
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
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@DisplayName("A ControlComponentCodeSharesPayload")
class ControlComponentCodeSharePayloadTest extends MapperSetUp {

	private static final String TENANT_ID = "100";
	private static final String ELECTION_EVENT_ID = "4b7a8f063b564dbf8e24420d3f52f54f";
	private static final String VERIFICATION_CARD_SET_ID = "cbf8ac1c1bcf444da0ccf5d7e956153b";
	private static final int CHUNK_ID = 1;

	private static final SecureRandom secureRandom = new SecureRandom();

	private static ControlComponentCodeSharesPayload responsePayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setUpAll() throws JsonProcessingException {
		responsePayload = SerializationTestData.getResponsePayload(ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, CHUNK_ID);

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
		final ControlComponentCodeSharesPayload deserializedResponsePayload = mapper
				.readValue(rootNode.toString(), ControlComponentCodeSharesPayload.class);

		assertEquals(responsePayload, deserializedResponsePayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final ControlComponentCodeSharesPayload deserializedResponsePayload = mapper
				.readValue(mapper.writeValueAsString(responsePayload), ControlComponentCodeSharesPayload.class);

		assertEquals(responsePayload, deserializedResponsePayload);
	}

	@Nested
	@DisplayName("Constructing a ControlComponentCodeSharesPayload with")
	class ControlComponentCodeSharesPayloadConsistencyCheckTest {

		private String tenantId;
		private String electionEventId;
		private String verificationCardSetId;
		private int chunkId;
		private GqGroup encryptionGroup;
		private List<ControlComponentCodeShare> controlComponentCodeShares;
		private int nodeId;

		@BeforeEach
		void setup() {
			tenantId = "100";
			electionEventId = ELECTION_EVENT_ID;
			verificationCardSetId = VERIFICATION_CARD_SET_ID;
			encryptionGroup = SerializationTestData.getGqGroup();
			controlComponentCodeShares = Arrays
					.asList(SerializationTestData.getReturnCodeGenerationOutput("1ecb40f5bab5400e8166b63a04a0708d"),
							SerializationTestData.getReturnCodeGenerationOutput("2ecb40f5bab5400e8166b63a04a0708d"));
			chunkId = secureRandom.nextInt(5);
			nodeId = secureRandom.nextInt(4) + 1;
		}

		@Test
		@DisplayName("null parameters throws NullPointerException")
		void constructWithNullParameters() {
			assertAll(
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentCodeSharesPayload(null, verificationCardSetId, chunkId, encryptionGroup,
									controlComponentCodeShares, nodeId)),
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentCodeSharesPayload(electionEventId, null, chunkId, encryptionGroup,
									controlComponentCodeShares, nodeId)),
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, null,
									controlComponentCodeShares, nodeId)),
					() -> assertThrows(NullPointerException.class,
							() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, encryptionGroup,
									null, nodeId))
			);
		}

		@Test
		@DisplayName("nodeId out of range throws IllegalArgumentException")
		void constructWithBadNodeId() {
			final IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, encryptionGroup,
							controlComponentCodeShares, 0));
			assertEquals(String.format("The node id must be part of the known node ids. [nodeId: %s]", 0),
					Throwables.getRootCause(exception1).getMessage());

			final IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, encryptionGroup,
							controlComponentCodeShares, 5));
			assertEquals(String.format("The node id must be part of the known node ids. [nodeId: %s]", 5),
					Throwables.getRootCause(exception2).getMessage());
		}

		@Test
		@DisplayName("empty list of ControlComponentCodeShares throws IllegalArgumentException")
		void constructWithEmptyControlComponentCodeSharesList() {
			final List<ControlComponentCodeShare> emptyControlComponentCodeShares = List.of();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, encryptionGroup,
							emptyControlComponentCodeShares, nodeId));
			assertEquals("The list of control component code shares must not be empty.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("list of ControlComponentCodeShares containing null elements throws NullPointerException")
		void constructWithControlComponentCodeSharesListContainingNull() {
			final List<ControlComponentCodeShare> controlComponentCodeSharesWithNull = Arrays
					.asList(SerializationTestData.getReturnCodeGenerationOutput("1ecb40f5bab5400e8166b63a04a0708d"),
							SerializationTestData.getReturnCodeGenerationOutput("2ecb40f5bab5400e8166b63a04a0708d"), null);
			assertThrows(NullPointerException.class,
					() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, encryptionGroup,
							controlComponentCodeSharesWithNull, nodeId));
		}

		@Test
		@DisplayName("the ControlComponentCodeShares' groups different from the encryption group throws IllegalArgumentException")
		void constructWithInconsistentGroups() {
			final GqGroup otherEncryptionGroup = GroupTestData.getDifferentGqGroup(encryptionGroup);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, otherEncryptionGroup,
							controlComponentCodeShares, nodeId));
			assertEquals("The groups of the ControlComponentCodeShares must correspond to the encryption group.",
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("the ControlComponentCodeShares' having the same verification card IDs throws IllegalArgumentException")
		void constructWithIndistinctVerificationCardIds() {
			final List<ControlComponentCodeShare> controlComponentCodeSharesWithDuplicate = Arrays
					.asList(SerializationTestData.getReturnCodeGenerationOutput("1ecb40f5bab5400e8166b63a04a0708d"),
							SerializationTestData.getReturnCodeGenerationOutput("2ecb40f5bab5400e8166b63a04a0708d"),
							SerializationTestData.getReturnCodeGenerationOutput("2ecb40f5bab5400e8166b63a04a0708d"));
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, encryptionGroup,
							controlComponentCodeSharesWithDuplicate, nodeId));
			assertEquals("The verification card IDs must all be distinct.", Throwables.getRootCause(exception).getMessage());
		}
	}
}
