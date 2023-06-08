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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.google.common.base.Throwables;

@DisplayName("A combined correctness information")
class CombinedCorrectnessInformationTest {

	private static final String BALLOT_JSON = "ballot.json";
	private static final String BALLOT_2_JSON = "ballot2.json";
	private static final String BALLOT_3_JSON = "ballot3.json";
	private static final String BALLOT_4_JSON = "ballot4.json";
	private static final String RESOURCES_FOLDER_NAME = "CombinedCorrectnessInformationTest";

	private static Ballot ballot;
	private static Ballot ballot2;
	private static Ballot ballot3;
	private static Ballot ballot4;

	private static CombinedCorrectnessInformation combinedCorrectnessInformation;
	private static CombinedCorrectnessInformation combinedCorrectnessInformation2;
	private static CombinedCorrectnessInformation combinedCorrectnessInformation3;
	private static CombinedCorrectnessInformation combinedCorrectnessInformation4;

	@BeforeAll
	static void setUpAll() throws IOException {
		ballot = getBallotFromResourceName(BALLOT_JSON);
		ballot3 = getBallotFromResourceName(BALLOT_3_JSON);
		ballot2 = getBallotFromResourceName(BALLOT_2_JSON);
		ballot4 = getBallotFromResourceName(BALLOT_4_JSON);

		combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);
		combinedCorrectnessInformation2 = new CombinedCorrectnessInformation(ballot2);
		combinedCorrectnessInformation3 = new CombinedCorrectnessInformation(ballot3);
		combinedCorrectnessInformation4 = new CombinedCorrectnessInformation(ballot4);
	}

	private static Stream<Arguments> getTotalNumberOfSelectionsTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 13),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 3), Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 11),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 6));
	}

	private static Stream<Arguments> getTotalNumberOfVotingOptionsTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 432),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 10), Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 124),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 25));
	}

	private static Stream<Arguments> getCorrectnessIdForSelectionIndexTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 0, "e7c8b3ac09f64d95b08a6f451e0608fe"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 1, "acc6fffa007e413d8c2c51f80039f810"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 12, "acc6fffa007e413d8c2c51f80039f810"),

				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 0, "2d22a5bfa0f0406a9812576f925e1cea"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 1, "8522fc66faf2452e8062abe247ef5a24"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 2, "4c432e954efa4d8dbc5c5a8416d2e054"),

				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 0, "e727a2d916774758bd0c6f256c5ae241"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 1, "e65ce1ebeb9840a79c952a6ceae4c681"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 7, "e65ce1ebeb9840a79c952a6ceae4c681"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 8, "117bad3080214e94afe1abc811ebb2fb"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 9, "afaf857e8612471aa019f4b4ae0c4cd5"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 10, "afaf857e8612471aa019f4b4ae0c4cd5"),

				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 0, "b3053227e0b144b0ae4126f7ce24f4ab"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 1, "7340e7a7697c4223a7aade1a37b1575b"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 2, "65ee9ef89a9d413fa4efd6cfc4e69011"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 3, "235c5ea9ff57489aa193608dcc802328"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 4, "54013b5029d84b0cbb31349d4f68a27a"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 5, "54013b5029d84b0cbb31349d4f68a27a")

		);
	}

	private static Stream<Arguments> getCorrectnessIdForSelectionIndexOutOfBoundInputTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 13),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 3), Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 11),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 6));
	}

	private static Stream<Arguments> getCorrectnessIdForVotingOptionIndexTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 0, "e7c8b3ac09f64d95b08a6f451e0608fe"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 1, "e7c8b3ac09f64d95b08a6f451e0608fe"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 23, "e7c8b3ac09f64d95b08a6f451e0608fe"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 24, "acc6fffa007e413d8c2c51f80039f810"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 55, "acc6fffa007e413d8c2c51f80039f810"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 282, "acc6fffa007e413d8c2c51f80039f810"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 430, "acc6fffa007e413d8c2c51f80039f810"),
				Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 431, "acc6fffa007e413d8c2c51f80039f810"),

				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 0, "2d22a5bfa0f0406a9812576f925e1cea"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 1, "2d22a5bfa0f0406a9812576f925e1cea"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 2, "2d22a5bfa0f0406a9812576f925e1cea"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 3, "8522fc66faf2452e8062abe247ef5a24"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 4, "8522fc66faf2452e8062abe247ef5a24"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 5, "8522fc66faf2452e8062abe247ef5a24"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 6, "4c432e954efa4d8dbc5c5a8416d2e054"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 7, "4c432e954efa4d8dbc5c5a8416d2e054"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 8, "4c432e954efa4d8dbc5c5a8416d2e054"),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 9, "4c432e954efa4d8dbc5c5a8416d2e054"),

				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 0, "e727a2d916774758bd0c6f256c5ae241"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 1, "e727a2d916774758bd0c6f256c5ae241"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 7, "e727a2d916774758bd0c6f256c5ae241"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 8, "e65ce1ebeb9840a79c952a6ceae4c681"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 9, "e65ce1ebeb9840a79c952a6ceae4c681"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 105, "e65ce1ebeb9840a79c952a6ceae4c681"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 106, "e65ce1ebeb9840a79c952a6ceae4c681"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 107, "117bad3080214e94afe1abc811ebb2fb"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 108, "117bad3080214e94afe1abc811ebb2fb"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 113, "117bad3080214e94afe1abc811ebb2fb"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 114, "afaf857e8612471aa019f4b4ae0c4cd5"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 122, "afaf857e8612471aa019f4b4ae0c4cd5"),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 123, "afaf857e8612471aa019f4b4ae0c4cd5"),

				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 0, "b3053227e0b144b0ae4126f7ce24f4ab"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 2, "b3053227e0b144b0ae4126f7ce24f4ab"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 3, "7340e7a7697c4223a7aade1a37b1575b"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 5, "7340e7a7697c4223a7aade1a37b1575b"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 6, "65ee9ef89a9d413fa4efd6cfc4e69011"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 8, "65ee9ef89a9d413fa4efd6cfc4e69011"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 9, "235c5ea9ff57489aa193608dcc802328"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 11, "235c5ea9ff57489aa193608dcc802328"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 12, "54013b5029d84b0cbb31349d4f68a27a"),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 24, "54013b5029d84b0cbb31349d4f68a27a")

		);
	}

	private static Stream<Arguments> getCorrectnessIdForVotingOptionIndexOutOfBoundInputTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 432),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 10), Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 124),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 25));
	}

	private static Stream<Arguments> combinedCorrectnessInformationFromJsonTestSource() {
		return Stream.of(
				Arguments.of("combinedCorrectnessInformation.json", 0, "e7c8b3ac09f64d95b08a6f451e0608fe", 1, "e7c8b3ac09f64d95b08a6f451e0608fe", 13,
						432, Collections.emptyList(), 0),
				Arguments.of("combinedCorrectnessInformation2.json", 0, "2d22a5bfa0f0406a9812576f925e1cea", 1, "2d22a5bfa0f0406a9812576f925e1cea", 3,
						10, List.of(BigInteger.valueOf(113L)), 1),
				Arguments.of("combinedCorrectnessInformation3.json", 0, "e727a2d916774758bd0c6f256c5ae241", 1, "e727a2d916774758bd0c6f256c5ae241", 11,
						124, Collections.emptyList(), 0),
				Arguments.of("combinedCorrectnessInformation4.json", 0, "b3053227e0b144b0ae4126f7ce24f4ab", 1, "b3053227e0b144b0ae4126f7ce24f4ab", 6,
						25, List.of(BigInteger.valueOf(167L), BigInteger.valueOf(257L)), 2)

		);
	}

	private static Stream<Arguments> combinedCorrectnessInformationJsonMatchingTestSource() {
		return Stream.of(Arguments.of("ballot.json", ballot, "combinedCorrectnessInformation.json"),
				Arguments.of("ballot2.json", ballot2, "combinedCorrectnessInformation2.json"),
				Arguments.of("ballot3.json", ballot3, "combinedCorrectnessInformation3.json"),
				Arguments.of("ballot4.json", ballot4, "combinedCorrectnessInformation4.json")

		);
	}

	private static Ballot getBallotFromResourceName(final String resourceName) throws IOException {
		return new ObjectMapper().readValue(
				CombinedCorrectnessInformationTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME + File.separator + resourceName),
				Ballot.class);
	}

	private static CombinedCorrectnessInformation getCombinedCorrectnessInformationFromJson(final String jsonFilename) throws IOException {
		return new ObjectMapper().readValue(
				CombinedCorrectnessInformationTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME + File.separator + jsonFilename),
				CombinedCorrectnessInformation.class);
	}

	private static Stream<Arguments> getCorrectnessInformationSelectionsTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 13),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 3), Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 11),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 6));
	}

	private static Stream<Arguments> getCorrectnessInformationVotingOptionsTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, combinedCorrectnessInformation, 432),
				Arguments.of(BALLOT_2_JSON, combinedCorrectnessInformation2, 10),
				Arguments.of(BALLOT_3_JSON, combinedCorrectnessInformation3, 124),
				Arguments.of(BALLOT_4_JSON, combinedCorrectnessInformation4, 25));
	}

	@ParameterizedTest(name = "built from {0} expected value {2}.")
	@MethodSource("getTotalNumberOfSelectionsTestSource")
	@DisplayName("built from a valid ballot, calling getTotalNumberOfSelections returns the expected result.")
	void getTotalNumberOfSelectionsTest(final String ignoredBallotName, final CombinedCorrectnessInformation combinedCorrectnessInformation,
			final int expectedTotalNumberOfSelections) {

		assertEquals(expectedTotalNumberOfSelections, combinedCorrectnessInformation.getTotalNumberOfSelections());
	}

	@ParameterizedTest(name = "built from {0} expected value {2}.")
	@MethodSource("getTotalNumberOfVotingOptionsTestSource")
	@DisplayName("built from a valid ballot, calling getTotalNumberOfVotingOptions returns the expected result.")
	void getTotalNumberOfVotingOptionsTest(final String ignoredBallotName, final CombinedCorrectnessInformation combinedCorrectnessInformation,
			final int expectedTotalNumberOfVotingOptions) {

		assertEquals(expectedTotalNumberOfVotingOptions, combinedCorrectnessInformation.getTotalNumberOfVotingOptions());
	}

	@ParameterizedTest(name = "built from {0} and given index {2}, expected value {3}.")
	@MethodSource("getCorrectnessIdForSelectionIndexTestSource")
	@DisplayName("built from a valid ballot, calling getCorrectnessIdForSelectionIndex with a valid index returns the expected result.")
	void getCorrectnessIdForSelectionIndexTest(final String ignoredBallotName, final CombinedCorrectnessInformation combinedCorrectnessInformation,
			final int index, final String expectedCorrectnessId) {

		assertEquals(expectedCorrectnessId, combinedCorrectnessInformation.getCorrectnessIdForSelectionIndex(index));
	}

	@Test
	@DisplayName("built from a valid ballot, calling getCorrectnessIdForSelectionIndex with a negative index throws an IllegalArgumentException.")
	void getCorrectnessIdForSelectionIndexNegativeInputTest() {
		final int index = -1;
		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);

		final IllegalArgumentException negativeIndexException = assertThrows(IllegalArgumentException.class,
				() -> combinedCorrectnessInformation.getCorrectnessIdForSelectionIndex(index));

		assertEquals("The provided index " + index + " is negative.", negativeIndexException.getMessage());
	}

	@ParameterizedTest(name = "built from {0} and given index {2}.")
	@MethodSource("getCorrectnessIdForSelectionIndexOutOfBoundInputTestSource")
	@DisplayName("built from a valid ballot, calling getCorrectnessIdForSelectionIndex with an out of bound index throws an IllegalArgumentException.")
	void getCorrectnessIdForSelectionIndexOutOfBoundInputTest(final String ignoredBallotName,
			final CombinedCorrectnessInformation combinedCorrectnessInformation, final int index) {

		final IllegalArgumentException outOfBoundIndexException = assertThrows(IllegalArgumentException.class,
				() -> combinedCorrectnessInformation.getCorrectnessIdForSelectionIndex(index));

		assertEquals("There are less correctnessIds than the provided index " + index + ".", outOfBoundIndexException.getMessage());
	}

	@ParameterizedTest(name = "built from {0} and given index {2}, expected value {3}.")
	@MethodSource("getCorrectnessIdForVotingOptionIndexTestSource")
	@DisplayName("built from a valid ballot, calling getCorrectnessIdForVotingOptionIndex with a valid index returns the expected result.")
	void getCorrectnessIdForVotingOptionIndexTest(final String ignoredBallotName, final CombinedCorrectnessInformation combinedCorrectnessInformation,
			final int index, final String expectedCorrectnessId) {
		assertEquals(expectedCorrectnessId, combinedCorrectnessInformation.getCorrectnessIdForVotingOptionIndex(index));
	}

	@Test
	@DisplayName("built from a valid ballot, calling getCorrectnessIdForVotingOptionIndex with a negative index throws an IllegalArgumentException.")
	void getCorrectnessIdForVotingOptionIndexNegativeInputTest() {
		final int index = -1;
		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);

		final IllegalArgumentException negativeIndexException = assertThrows(IllegalArgumentException.class,
				() -> combinedCorrectnessInformation.getCorrectnessIdForVotingOptionIndex(index));

		assertEquals("The provided index " + index + " is negative.", negativeIndexException.getMessage());
	}

	@ParameterizedTest(name = "built from {0} and given index {2}.")
	@MethodSource("getCorrectnessIdForVotingOptionIndexOutOfBoundInputTestSource")
	@DisplayName("built from a valid ballot, calling getCorrectnessIdForVotingOptionIndex with an out of bound index throws an IllegalArgumentException.")
	void getCorrectnessIdForVotingOptionIndexOutOfBoundInputTest(final String ignoredBallotName,
			final CombinedCorrectnessInformation combinedCorrectnessInformation, final int index) {

		final IllegalArgumentException outOfBoundIndexException = assertThrows(IllegalArgumentException.class,
				() -> combinedCorrectnessInformation.getCorrectnessIdForVotingOptionIndex(index));

		assertEquals("There are less voting options than the provided index " + index + ".", outOfBoundIndexException.getMessage());
	}

	@Test
	@DisplayName("built from a malformed ballot with an exceeded questions size throws.")
	void exceededQuestionsSizeIllegalArgumentExceptionTest() {
		final ValueInstantiationException exception = assertThrows(
				ValueInstantiationException.class, () -> getBallotFromResourceName("ballotExceededQuestionsSize.json"));

		assertTrue(Throwables.getRootCause(exception).getMessage().startsWith("The given string does not comply with the required format."));
	}

	@Test
	@DisplayName("built from a malformed ballot with an attribute without a related question, throws an IllegalArgumentException.")
	void attributeWithoutRelatedQuestionIllegalArgumentExceptionTest() throws IOException {
		final Ballot ballotNoCorrespondingAttributeQuestion = getBallotFromResourceName("ballotNoCorrespondingAttributeQuestion.json");

		final IllegalArgumentException illegalArgumentException = assertThrows(
				IllegalArgumentException.class, () -> new CombinedCorrectnessInformation(ballotNoCorrespondingAttributeQuestion));

		assertEquals(
				"No corresponding question to attribute found in contest. [contestId: f64da23c11e641a1ac1defa897c3d279, attributeId: 2d22a5bfa0f0406a9812576f925e1ceb].",
				illegalArgumentException.getMessage());
	}

	@Test
	@DisplayName("built from a malformed ballot with no contests, throws an IllegalArgumentException.")
	void noContestsIllegalArgumentExceptionTest() throws IOException {
		final ValueInstantiationException exception = assertThrows(ValueInstantiationException.class,
				() -> getBallotFromResourceName("ballotNoContests.json"));

		assertEquals(NullPointerException.class, exception.getCause().getClass());
	}

	@Test
	@DisplayName("built from a malformed ballot with empty contests throws.")
	void emptyContestsIllegalArgumentExceptionTest() {
		final ValueInstantiationException exception = assertThrows(
				ValueInstantiationException.class, () -> getBallotFromResourceName("ballotEmptyContests.json"));

		assertEquals("The ballot must contain at least one contest. [ballotId: a5c0305db01142e786533cb48df1c794]",
				Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("built from a malformed ballot with a contest with no questions, throws an IllegalArgumentException.")
	void noQuestionsIllegalArgumentExceptionTest() {
		final ValueInstantiationException exception = assertThrows(
				ValueInstantiationException.class, () -> getBallotFromResourceName("ballotContestNoQuestions.json"));

		assertEquals(NullPointerException.class, exception.getCause().getClass());
	}

	@Test
	@DisplayName("built from a malformed ballot with a contest with empty questions, throws an IllegalArgumentException.")
	void emptyQuestionsIllegalArgumentExceptionTest() {
		final ValueInstantiationException exception = assertThrows(ValueInstantiationException.class,
				() -> getBallotFromResourceName("ballotContestEmptyQuestions.json"));

		assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
	}

	@Test
	@DisplayName("built from a valid ballot and mapped to a JSON string, does not throw an exception.")
	void combinedCorrectnessInformationToJsonTest() {

		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);

		assertDoesNotThrow(() -> new ObjectMapper().writeValueAsString(combinedCorrectnessInformation));
	}

	@ParameterizedTest(name = "built from {0}.")
	@MethodSource("combinedCorrectnessInformationJsonMatchingTestSource")
	@DisplayName("built from a valid ballot and mapped to a JSON string, matches the expected JSON string.")
	void combinedCorrectnessInformationJsonMatchingTest(final String ignoredBallotJsonFileName, final Ballot ballot,
			final String combinedCorrectnessInformationJsonFileName) throws IOException {

		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);
		final CombinedCorrectnessInformation combinedCorrectnessInformationRebuiltFromJSON = getCombinedCorrectnessInformationFromJson(
				combinedCorrectnessInformationJsonFileName);

		assertEquals(new ObjectMapper().writeValueAsString(combinedCorrectnessInformationRebuiltFromJSON),
				new ObjectMapper().writeValueAsString(combinedCorrectnessInformation));
	}

	@ParameterizedTest(name = "built from {0}.")
	@MethodSource("combinedCorrectnessInformationFromJsonTestSource")
	@DisplayName("built from a JSON string representation, returns the expected results upon methods calls.")
	void combinedCorrectnessInformationFromJsonTest(final String jsonFileName, final int selectionIndex,
			final String expectedCorrectnessIdForSelectionIndex, final int votingOptionIndex, final String expectedCorrectnessIdForVotingOptionIndex,
			final int expectedTotalNumberOfSelections, final int expectedTotalNumberOfVotingOptions,
			final List<BigInteger> expectedTotalListOfWriteInOptions, final int expectedTotalNumberOfWriteInOptions) throws IOException {

		final CombinedCorrectnessInformation combinedCorrectnessInformationRebuiltFromJSON = getCombinedCorrectnessInformationFromJson(jsonFileName);

		assertAll(() -> assertNotNull(combinedCorrectnessInformationRebuiltFromJSON),
				() -> assertNotNull(combinedCorrectnessInformationRebuiltFromJSON.getCorrectnessInformationList()),
				() -> assertEquals(expectedCorrectnessIdForSelectionIndex,
						combinedCorrectnessInformationRebuiltFromJSON.getCorrectnessIdForSelectionIndex(selectionIndex)),
				() -> assertEquals(expectedCorrectnessIdForVotingOptionIndex,
						combinedCorrectnessInformationRebuiltFromJSON.getCorrectnessIdForVotingOptionIndex(votingOptionIndex)),
				() -> assertEquals(expectedTotalNumberOfSelections, combinedCorrectnessInformationRebuiltFromJSON.getTotalNumberOfSelections()),
				() -> assertEquals(expectedTotalNumberOfVotingOptions,
						combinedCorrectnessInformationRebuiltFromJSON.getTotalNumberOfVotingOptions()),
				() -> assertEquals(expectedTotalListOfWriteInOptions, combinedCorrectnessInformationRebuiltFromJSON.getTotalListOfWriteInOptions()),
				() -> assertEquals(expectedTotalNumberOfWriteInOptions,
						combinedCorrectnessInformationRebuiltFromJSON.getTotalNumberOfWriteInOptions()));
	}

	@Test
	@DisplayName("built from a valid ballot, calling equals method returns the expected result.")
	void equalsTest() {
		assertAll(() -> assertEquals(combinedCorrectnessInformation, new CombinedCorrectnessInformation(ballot)),
				() -> assertEquals(combinedCorrectnessInformation, combinedCorrectnessInformation)
		);
	}

	@ParameterizedTest(name = "built from {0} expected value {2}.")
	@MethodSource("getCorrectnessInformationSelectionsTestSource")
	@DisplayName("built from a valid ballot, calling getCorrectnessInformationSelections returns the expected result.")
	void getCorrectnessInformationSelectionsTest(final String ignoredBallotName, final CombinedCorrectnessInformation combinedCorrectnessInformation,
			final int expectedNumberOfSelections) {

		assertEquals(expectedNumberOfSelections, combinedCorrectnessInformation.getCorrectnessInformationSelections().size());
	}

	@ParameterizedTest(name = "built from {0} expected value {2}.")
	@MethodSource("getCorrectnessInformationVotingOptionsTestSource")
	@DisplayName("built from a valid ballot, calling getCorrectnessInformationVotingOptions returns the expected result.")
	void getCorrectnessInformationVotingOptionsTest(final String ignoredBallotName,
			final CombinedCorrectnessInformation combinedCorrectnessInformation,
			final int expectedNumberOfVotingOptions) {

		assertEquals(expectedNumberOfVotingOptions, combinedCorrectnessInformation.getCorrectnessInformationVotingOptions().size());
	}
}
