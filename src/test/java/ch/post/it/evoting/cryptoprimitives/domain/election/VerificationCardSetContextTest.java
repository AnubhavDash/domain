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
package ch.post.it.evoting.cryptoprimitives.domain.election;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.validations.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;

@DisplayName("Constructing a VerificationCardSetContext with")
class VerificationCardSetContextTest {

	private static final int UUID_LENGTH = 32;
	private static final Random random = RandomFactory.createRandom();

	private String verificationCardSetId;
	private String verificationCardSetAlias;
	private String verificationCardSetDescription;
	private String ballotBoxId;
	private LocalDateTime ballotBoxStartTime;
	private LocalDateTime ballotBoxFinishTime;
	private boolean testBallotBox;
	private int numberOfWriteInFields;
	private int numberOfVotingCards;
	private int gracePeriod;
	private PrimesMappingTable primesMappingTable;

	@BeforeEach
	void setup() {
		verificationCardSetId = random.genRandomBase16String(UUID_LENGTH);
		verificationCardSetAlias = random.genRandomBase16String(UUID_LENGTH);
		verificationCardSetDescription = random.genRandomBase16String(UUID_LENGTH);
		ballotBoxId = random.genRandomBase16String(UUID_LENGTH);
		ballotBoxStartTime = LocalDateTime.now();
		ballotBoxFinishTime = ballotBoxStartTime.plusYears(5);
		testBallotBox = Math.random() < 0.5;
		numberOfWriteInFields = 1;
		numberOfVotingCards = 10;
		gracePeriod = 900;
	}

	@Test
	@DisplayName("null arguments throws a NullPointerException")
	void nullArgumentsThrows() {
		assertThrows(NullPointerException.class,
				() -> new VerificationCardSetContext(null, verificationCardSetAlias, verificationCardSetDescription, ballotBoxId, ballotBoxStartTime,
						ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod, primesMappingTable));
		assertThrows(NullPointerException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, null, verificationCardSetDescription, ballotBoxId, ballotBoxStartTime,
						ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod, primesMappingTable));
		assertThrows(NullPointerException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, null, ballotBoxId, ballotBoxStartTime,
						ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod, primesMappingTable));
		assertThrows(NullPointerException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription, null,
						ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod,
						primesMappingTable));
		assertThrows(NullPointerException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription, ballotBoxId,
						null, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod, primesMappingTable));
		assertThrows(NullPointerException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription, ballotBoxId,
						ballotBoxStartTime, null, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod, primesMappingTable));
		assertThrows(NullPointerException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription, ballotBoxId,
						ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod, null));
	}

	@Test
	@DisplayName("invalid identifiers throws a FailedValidationException")
	void invalidIdentifiersThrows() {
		final String invalidVerificationCardSetId = verificationCardSetId.substring(1) + ".";
		final String invalidBallotBoxId = ballotBoxId.substring(1) + "_";
		assertThrows(FailedValidationException.class,
				() -> new VerificationCardSetContext(invalidVerificationCardSetId, verificationCardSetAlias, verificationCardSetDescription,
						ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod,
						primesMappingTable));
		assertThrows(FailedValidationException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription,
						invalidBallotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod,
						primesMappingTable));
	}

	@Test
	@DisplayName("blank verification card set alias throws an IllegalArgumentException")
	void blankVerificationCardSetAliasThrows() {
		assertThrows(IllegalArgumentException.class, () -> new VerificationCardSetContext(verificationCardSetId, " \t", verificationCardSetDescription,
				ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod,
				primesMappingTable));
	}

	@Test
	@DisplayName("non-UTF-8 verification card set alias throws a FailedValidationException")
	void nonUtf8VerificationCardSetAliasThrows() {
		assertThrows(FailedValidationException.class, () -> new VerificationCardSetContext(verificationCardSetId, "\uDFFF", verificationCardSetDescription,
				ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod,
				primesMappingTable));
	}

	@Test
	@DisplayName("blank verification card set description throws an IllegalArgumentException")
	void blankVerificationCardSetDescriptionThrows() {
		assertThrows(IllegalArgumentException.class, () -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, " \t",
				ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod,
				primesMappingTable));
	}

	@Test
	@DisplayName("non-UTF-8 verification card set description throws a FailedValidationException")
	void nonUtf8VerificationCardSetDescriptionThrows() {
		assertThrows(FailedValidationException.class, () -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, "\uDFFF",
				ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, gracePeriod,
				primesMappingTable));
	}

	@Test
	@DisplayName("ballot box start time after ballot box finish time throws an IllegalArgumentException")
	void startTimeAfterFinishTimeThrows() {
		final LocalDateTime tooLateBallotBoxStartTime = ballotBoxFinishTime.plusNanos(1);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription,
						ballotBoxId, tooLateBallotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards,
						gracePeriod, primesMappingTable));
		assertEquals("The ballot box start time must not be after the ballot box finish time.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("negative number of write-in fields throws an IllegalArgumentException")
	void negativeNumberOfWriteInFieldsThrows() {
		final int negativeNumberOfWriteInFields = -1;
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription,
				ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, negativeNumberOfWriteInFields, numberOfVotingCards, gracePeriod,
				primesMappingTable));
		assertEquals("The number of write-in fields must be positive.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("zero voting cards throws an IllegalArgumentException")
	void zeroVotingCardsThrows() {
		final int zeroNumberOfVotingCards = 0;
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription,
				ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, zeroNumberOfVotingCards, gracePeriod,
				primesMappingTable));
		assertEquals("The number of voting cards must be strictly positive.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("negative grace period throws an IllegalArgumentException")
	void negativeGracePeriodThrows() {
		final int negativeGracePeriod = -1;
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new VerificationCardSetContext(verificationCardSetId, verificationCardSetAlias, verificationCardSetDescription,
				ballotBoxId, ballotBoxStartTime, ballotBoxFinishTime, testBallotBox, numberOfWriteInFields, numberOfVotingCards, negativeGracePeriod,
				primesMappingTable));
		assertEquals("The grace period must be positive.", Throwables.getRootCause(exception).getMessage());
	}
}
