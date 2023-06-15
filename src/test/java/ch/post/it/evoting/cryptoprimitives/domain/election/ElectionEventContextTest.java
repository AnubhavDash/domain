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
package ch.post.it.evoting.cryptoprimitives.domain.election;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.validations.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;

class ElectionEventContextTest {

	private static final int UUID_LENGTH = 32;
	private static final Random RANDOM = RandomFactory.createRandom();

	private String electionEventId;
	private String electionEventAlias;
	private String electionEventDescription;
	private List<VerificationCardSetContext> verificationCardSetContexts;
	private LocalDateTime startTime;
	private LocalDateTime finishTime;

	@BeforeEach
	void setup() {
		electionEventId = RANDOM.genRandomBase16String(UUID_LENGTH);
		electionEventAlias = electionEventId + "_alias";
		electionEventDescription = electionEventId + "_description";
		verificationCardSetContexts = List.of(generateVerificationCardSetContext());
		startTime = LocalDateTime.now();
		finishTime = startTime.plusYears(2);
	}

	@Test
	@DisplayName("with null arguments throws a NullPointerException")
	void constructWithNullArgumentsThrows() {
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(null, electionEventAlias, electionEventDescription, verificationCardSetContexts, startTime,
						finishTime));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, null, electionEventDescription, verificationCardSetContexts, startTime, finishTime));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, electionEventAlias, null, verificationCardSetContexts, startTime, finishTime));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, null, startTime, finishTime));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, verificationCardSetContexts, null,
						finishTime));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, verificationCardSetContexts, startTime,
						null));
	}

	@Test
	@DisplayName("with invalid election event id throws a FailedValidationException")
	void constructWithInvalidElectionEventIdThrows() {
		final String nonUuid = "non UUID!";
		assertThrows(FailedValidationException.class,
				() -> new ElectionEventContext(nonUuid, electionEventAlias, electionEventDescription, verificationCardSetContexts, startTime,
						finishTime));
	}

	@Test
	@DisplayName("with blank election event alias or description throws a IllegalArgumentException")
	void constructWithBlankAliasOrDescriptionThrows() {
		final String blank = " \t";
		assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, blank, electionEventDescription, verificationCardSetContexts, startTime,
						finishTime));
		assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, electionEventAlias, blank, verificationCardSetContexts, startTime,
						finishTime));
	}

	@Test
	@DisplayName("with non UTF-8 election event alias or description throws a FailedValidationException")
	void constructWithNonUtf8AliasOrDescriptionThrows() {
		final String nonUtf8 = "UTF-8 until here: \uDFFF";
		assertThrows(FailedValidationException.class,
				() -> new ElectionEventContext(electionEventId, nonUtf8, electionEventDescription, verificationCardSetContexts, startTime,
						finishTime));
		assertThrows(FailedValidationException.class,
				() -> new ElectionEventContext(electionEventId, electionEventAlias, nonUtf8, verificationCardSetContexts, startTime,
						finishTime));
	}

	private VerificationCardSetContext generateVerificationCardSetContext() {
		final String verificationCardSetId = RANDOM.genRandomBase16String(UUID_LENGTH);
		final String verificationCardSetAlias = verificationCardSetId + "_alias";
		final String ballotBoxId = RANDOM.genRandomBase16String(UUID_LENGTH);

		return generateVerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, ballotBoxId);
	}

	private VerificationCardSetContext generateVerificationCardSetContext(final String verificationCardSetId, final String verificationCardSetAlias,
			final String ballotBoxId) {
		final String verificationCardSetDescription = verificationCardSetId + "_description";
		final LocalDateTime startTime = LocalDateTime.now();
		final LocalDateTime finishTime = startTime.plusDays(5);
		final boolean testBallotBox = Math.random() < 0.5;
		final int numberOfWriteInFields = 1;
		final int numberVotingCards = 10;
		final int gracePeriod = 900;
		final GroupVector<PrimeGqElement, GqGroup> smallPrimeGroupMembers = PrimeGqElement.PrimeGqElementFactory.getSmallPrimeGroupMembers(
				SerializationTestData.getGqGroup(), 1);
		final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(
				List.of(new PrimesMappingTableEntry("actualVotingOption", smallPrimeGroupMembers.get(0), "semantic")));

		return new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription, ballotBoxId, startTime,
				finishTime, testBallotBox, numberOfWriteInFields, numberVotingCards,
				gracePeriod, primesMappingTable);
	}

	@Nested
	@DisplayName("with verification card set contexts")
	class VerificationCardSetContextsValidationTest {
		@Test
		@DisplayName("being empty throws an IllegalArgumentException")
		void constructWithEmptyVerificationCardSetContextsThrows() {
			final List<VerificationCardSetContext> emptyContexts = List.of();
			final IllegalArgumentException emptyIllegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, emptyContexts, startTime,
							finishTime));
			assertEquals("VerificationCardSetContexts cannot be empty.", emptyIllegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("containing duplicate ballot box ids throws an IllegalArgumentException")
		void constructWithDuplicateBallotBoxIdsThrows() {
			final ArrayList<VerificationCardSetContext> duplicateBallotBoxIds = new ArrayList<>();
			final VerificationCardSetContext verificationCardSetContextOne = verificationCardSetContexts.get(0);
			final String verificationCardSetId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final String verificationCardSetAlias = "this is an alias";
			final String ballotBoxId = verificationCardSetContextOne.ballotBoxId();
			final VerificationCardSetContext verificationCardSetContextTwo = generateVerificationCardSetContext(verificationCardSetId,
					verificationCardSetAlias, ballotBoxId);
			duplicateBallotBoxIds.add(verificationCardSetContextOne);
			duplicateBallotBoxIds.add(verificationCardSetContextTwo);
			final IllegalArgumentException duplicateVerificationCardSetIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, duplicateBallotBoxIds,
							startTime,
							finishTime));
			assertEquals("VerificationCardSetContexts cannot contain duplicate BallotBoxIds.",
					duplicateVerificationCardSetIdsIllegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("containing duplicate verification card set ids throws an IllegalArgumentException")
		void constructWithDuplicateVerificationCardSetIdsThrows() {
			final ArrayList<VerificationCardSetContext> duplicateVerificationCardSetIds = new ArrayList<>();
			final VerificationCardSetContext verificationCardSetContextOne = verificationCardSetContexts.get(0);
			final String verificationCardSetId = verificationCardSetContextOne.verificationCardSetId();
			final String verificationCardSetAlias = "this is an alias";
			final String ballotBoxId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final VerificationCardSetContext verificationCardSetContextTwo = generateVerificationCardSetContext(verificationCardSetId,
					verificationCardSetAlias, ballotBoxId);
			duplicateVerificationCardSetIds.add(verificationCardSetContextOne);
			duplicateVerificationCardSetIds.add(verificationCardSetContextTwo);
			final IllegalArgumentException duplicateVerificationCardSetIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, duplicateVerificationCardSetIds,
							startTime,
							finishTime));
			assertEquals("VerificationCardSetContexts cannot contain duplicate VerificationCardSetIds.",
					duplicateVerificationCardSetIdsIllegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("containing duplicate verification card set aliases throws an IllegalArgumentException")
		void constructWithDuplicateVerificationCardSetAliasesThrows() {
			final ArrayList<VerificationCardSetContext> duplicateVerificationCardSetAliases = new ArrayList<>();
			final VerificationCardSetContext verificationCardSetContextOne = verificationCardSetContexts.get(0);
			final String verificationCardSetId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final String verificationCardSetAlias = verificationCardSetContextOne.verificationCardSetAlias();
			final String ballotBoxId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final VerificationCardSetContext verificationCardSetContextTwo = generateVerificationCardSetContext(verificationCardSetId,
					verificationCardSetAlias, ballotBoxId);
			duplicateVerificationCardSetAliases.add(verificationCardSetContextOne);
			duplicateVerificationCardSetAliases.add(verificationCardSetContextTwo);
			final IllegalArgumentException duplicateVerificationCardSetIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
					() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, duplicateVerificationCardSetAliases,
							startTime,
							finishTime));
			assertEquals("VerificationCardSetContexts cannot contain duplicate VerificationCardSetAliases.",
					duplicateVerificationCardSetIdsIllegalArgumentException.getMessage());
		}

		@Test
		@DisplayName("with ballot box start time not between election event start and finish time throws an IllegalArgumentException")
		void constructWithTooEarlyBallotBoxStartTimeThrows() {
			final String verificationCardSetId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final String verificationCardSetAlias = verificationCardSetId + "_alias";
			final String verificationCardSetDescription = verificationCardSetId + "_description";
			final String ballotBoxId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final LocalDateTime ballotBoxStartTime = startTime.minusSeconds(1);
			final LocalDateTime ballotBoxFinishTime = finishTime.minusSeconds(1);
			final boolean testBallotBox = Math.random() < 0.5;
			final int numberOfWriteInFields = 1;
			final int numberVotingCards = 10;
			final int gracePeriod = 900;
			final GroupVector<PrimeGqElement, GqGroup> smallPrimeGroupMembers = PrimeGqElement.PrimeGqElementFactory.getSmallPrimeGroupMembers(
					SerializationTestData.getGqGroup(), 1);
			final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(
					List.of(new PrimesMappingTableEntry("actualVotingOption", smallPrimeGroupMembers.get(0), "semantic")));

			final VerificationCardSetContext verificationCardSetContext = new VerificationCardSetContext(verificationCardSetId,
					verificationCardSetAlias, verificationCardSetDescription, ballotBoxId, ballotBoxStartTime,
					ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberVotingCards,
					gracePeriod, primesMappingTable);
			final List<VerificationCardSetContext> tooEarlyBallotBoxStartTime = List.of(verificationCardSetContext);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, tooEarlyBallotBoxStartTime,
							startTime, finishTime));
			assertEquals("The ballot box start times must be between the election event start and finish times.",
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("with ballot box finish time not between election event start and finish time throws an IllegalArgumentException")
		void constructWithTooLateBallotBoxFinishTimeThrows() {
			final String verificationCardSetId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final String verificationCardSetAlias = verificationCardSetId + "_alias";
			final String verificationCardSetDescription = verificationCardSetId + "_description";
			final String ballotBoxId = RANDOM.genRandomBase16String(UUID_LENGTH);
			final LocalDateTime ballotBoxStartTime = startTime.plusSeconds(1);
			final LocalDateTime ballotBoxFinishTime = finishTime.plusSeconds(1);
			final boolean testBallotBox = Math.random() < 0.5;
			final int numberOfWriteInFields = 1;
			final int numberVotingCards = 10;
			final int gracePeriod = 900;
			final GroupVector<PrimeGqElement, GqGroup> smallPrimeGroupMembers = PrimeGqElement.PrimeGqElementFactory.getSmallPrimeGroupMembers(
					SerializationTestData.getGqGroup(), 1);
			final PrimesMappingTable primesMappingTable = PrimesMappingTable.from(
					List.of(new PrimesMappingTableEntry("actualVotingOption", smallPrimeGroupMembers.get(0), "semantic")));

			final VerificationCardSetContext verificationCardSetContext = new VerificationCardSetContext(verificationCardSetId,
					verificationCardSetAlias, verificationCardSetDescription, ballotBoxId, ballotBoxStartTime,
					ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberVotingCards,
					gracePeriod, primesMappingTable);
			final List<VerificationCardSetContext> tooLateBallotBoxFinishTime = List.of(verificationCardSetContext);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new ElectionEventContext(electionEventId, electionEventAlias, electionEventDescription, tooLateBallotBoxFinishTime,
							startTime, finishTime));
			assertEquals("The ballot box finish times must be between the election event start and finish times.",
					Throwables.getRootCause(exception).getMessage());
		}
	}
}
