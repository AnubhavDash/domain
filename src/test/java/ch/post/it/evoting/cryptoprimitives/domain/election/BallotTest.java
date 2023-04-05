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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
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

import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;

@DisplayName("A ballot")
class BallotTest {

	private static final String RESOURCES_FOLDER_NAME = "BallotTest";
	private static final String BALLOT_JSON = "ballot.json";
	private static final String BALLOT2_JSON = "ballot2.json";
	private static final String BALLOT3_JSON = "ballot3.json";
	private static final String BALLOT4_JSON = "ballot4.json";

	private static List<ElectionOption> electionOptions;
	private static List<ElectionOption> electionOptions2;

	@BeforeAll
	static void setUpAll() throws IOException {

		electionOptions = getBallotFromResourceName(BALLOT_JSON).getOrderedElectionOptions();
		electionOptions2 = getBallotFromResourceName(BALLOT2_JSON).getOrderedElectionOptions();
	}

	@Test
	@DisplayName("built from a ballot json file containing more fields than expected, does not throw any exception.")
	void canBeCreatedFromAJsonContainingMoreFieldsThanExpectedTest() {
		final Ballot ballot = assertDoesNotThrow(() -> getBallotFromResourceName(BALLOT3_JSON));
		assertNotNull(ballot);
	}

	@ParameterizedTest(name = "built from {0} and given index {2}, expected representation value {3}.")
	@MethodSource("getElectionOptionsTestSource")
	@DisplayName("built from a valid ballot json file, calling getOrderedElectionOptions returns the expected representation value.")
	void getElectionOptionsReturnsExpectedTest(final String ignoredBallotName, final List<ElectionOption> electionOptions, final int index,
			final String expectedRepresentation) {
		assertEquals(expectedRepresentation, electionOptions.get(index).getRepresentation());
	}

	@Test
	@DisplayName("built from a malformed ballot json file, calling getOrderedElectionOptions throws a ValueInstantiationException.")
	void getOrderedElectionOptionsThrowsIllegalArgumentExceptionTest() {
		final ValueInstantiationException exception = assertThrows(ValueInstantiationException.class,
				() -> getBallotFromResourceName(BALLOT4_JSON));

		assertEquals("The template must be either options or listsAndCandidates. [template: template, contestId: 690c5dd67c1045a2bdaf1d1104417fb0]",
				Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("built from a valid ballot json file, calling getEncodedVotingOptions returns the expected encoded voting options.")
	void getEncodedVotingOptionsReturnsExpectedTest() throws IOException {

		final Ballot ballot = getBallotFromResourceName(BALLOT_JSON);
		final List<Integer> expected = Arrays.asList(79, 47, 73, 37, 71, 59);

		assertEquals(expected, ballot.getEncodedVotingOptions());
	}

	@Test
	@DisplayName("built from a valid ballot json file, calling getActualVotingOptions returns the expected actual voting options.")
	void getActualVotingOptionsReturnsExpectedTest() throws IOException {

		final Ballot ballot = getBallotFromResourceName(BALLOT_JSON);
		final List<String> expected = List.of(
				"BLANK_f70dab48718545ed87d37dc7fc1590eb",
				"ce84154f-0a5a-3e93-85a6-92ec1986b6c6",
				"00af9ff3-ef95-3204-b5fe-137f73858f6b",
				"BLANK_968a760bc55642cd91805540bfeef161",
				"a56d1cb2-1578-3636-9537-ded89b4c4715",
				"3cbabd4b-6ee1-3546-93ce-54a128658d1d");

		assertEquals(expected, ballot.getActualVotingOptions());
	}

	@Test
	@DisplayName("calling getAttributeAlias throws Exception upon missing element and upon multiple matching elements.")
	void getActualVotingOptionThrows() {

		final String attribute = RandomFactory.createRandom().genRandomBase16String(32);

		final List<ElectionAttributes> attributes = List.of(
				new ElectionAttributes(attribute, "ignored", Collections.emptyList(), true),
				new ElectionAttributes(attribute, "ignored", Collections.emptyList(), true));

		// No matching element.
		assertThrows(NoSuchElementException.class,
				() -> Ballot.getAttributeAlias(RandomFactory.createRandom().genRandomBase16String(32), attributes));

		// Multiple matching elements.
		assertThrows(IllegalArgumentException.class, () -> Ballot.getAttributeAlias(attribute, attributes));
	}

	private static Ballot getBallotFromResourceName(final String resourceName) throws IOException {
		return new ObjectMapper().readValue(
				CombinedCorrectnessInformationTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME + File.separator + resourceName),
				Ballot.class);
	}

	private static Stream<Arguments> getElectionOptionsTestSource() {
		return Stream.of(Arguments.of(BALLOT_JSON, electionOptions, 0, "79"), Arguments.of(BALLOT_JSON, electionOptions, 1, "47"),
				Arguments.of(BALLOT_JSON, electionOptions, 2, "73"), Arguments.of(BALLOT_JSON, electionOptions, 3, "37"),
				Arguments.of(BALLOT_JSON, electionOptions, 4, "71"), Arguments.of(BALLOT_JSON, electionOptions, 5, "59"),

				Arguments.of(BALLOT2_JSON, electionOptions2, 0, "10271"), Arguments.of(BALLOT2_JSON, electionOptions2, 1, "188311"),
				Arguments.of(BALLOT2_JSON, electionOptions2, 24, "187513"), Arguments.of(BALLOT2_JSON, electionOptions2, 25, "187669")

		);
	}
}
