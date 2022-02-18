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

import static ch.post.it.evoting.cryptoprimitives.domain.election.BallotValidations.checkContestsNotNullAndNotEmpty;
import static ch.post.it.evoting.cryptoprimitives.domain.election.BallotValidations.checkNotNullAndNotEmpty;
import static ch.post.it.evoting.cryptoprimitives.domain.election.BallotValidations.checkQuestionsSizeOfListsAndCandidatesContest;
import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.stringToInteger;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates the information contained within a ballot.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ballot {

	private final String id;

	private final ElectionEvent electionEvent;

	private final List<Contest> contests;

	@JsonCreator
	public Ballot(
			@JsonProperty("id")
			final String id,
			@JsonProperty("electionEvent")
			final ElectionEvent electionEvent,
			@JsonProperty("contests")
			final List<Contest> contests) {

		this.id = id;
		this.electionEvent = electionEvent;
		this.contests = contests;
	}

	public String getId() {
		return id;
	}

	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	public List<Contest> getContests() {
		return contests;
	}

	/**
	 * Returns the voting options - encoded as prime numbers - for this ballot. We order the encoded voting options by two levels: First, by how the
	 * corresponding questions are displayed on the voter portal and second, by how the voting options appear in the "the {@link ElectionAttributes}
	 * object of each contest.
	 *
	 * @return the voting options encoded as prime numbers.
	 * @throws IllegalArgumentException if the ballot contains an unsupported {@link Contest} template. Supported templates are
	 *                                  <ul>
	 *                                      <li>{@value Contest#LISTS_AND_CANDIDATES_TEMPLATE}</li>
	 *                                      <li>{@value Contest#OPTIONS_TEMPLATE}</li>
	 *                                  </ul>
	 */
	@JsonIgnore
	public List<BigInteger> getEncodedVotingOptions() {
		return getOrderedElectionOptions().stream()
				.map(electionOption -> stringToInteger(electionOption.getRepresentation()))
				.collect(Collectors.toList());
	}

	/**
	 * Returns the ordered {@link ElectionOption}s for this ballot. We order the election options by two levels: First, by how the corresponding
	 * questions of each contest are displayed on the voter portal and second, by how the voting options appear in the {@link ElectionAttributes}
	 * object of each contest.
	 *
	 * @throws IllegalArgumentException if the ballot contains an unsupported {@link Contest} template. Supported templates are
	 *                                  <ul>
	 *                                      <li>{@value Contest#LISTS_AND_CANDIDATES_TEMPLATE}</li>
	 *                                      <li>{@value Contest#OPTIONS_TEMPLATE}</li>
	 *                                  </ul>
	 */
	@JsonIgnore
	public List<ElectionOption> getOrderedElectionOptions() {

		return checkContestsNotNullAndNotEmpty(this.contests, this.id).stream()
				.map(this::getOrderedElectionOptionsFromContest)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private List<ElectionOption> getOrderedElectionOptionsFromContest(final Contest contest) {
		final String contestId = contest.getId();
		final String template = contest.getTemplate();
		final List<Question> questions = contest.getQuestions();
		final List<ElectionAttributes> attributes = contest.getAttributes();
		final List<ElectionOption> electionOptions = contest.getOptions();

		checkNotNullAndNotEmpty(questions, "questions", contestId);
		checkNotNullAndNotEmpty(attributes, "election attributes", contestId);
		checkNotNullAndNotEmpty(electionOptions, "election options", contestId);

		if (Contest.OPTIONS_TEMPLATE.equals(template)) {
			return getOrderedElectionOptionsFromOptionsTemplateContest(attributes, electionOptions);

		} else if (Contest.LISTS_AND_CANDIDATES_TEMPLATE.equals(template)) {
			return getOrderedElectionOptionsFromListsAndCandidatesTemplateContest(questions, attributes, electionOptions, contestId);

		} else {
			throw new IllegalArgumentException(
					String.format("Contests with template \"%s\" are not supported. [contestId=%s]", template, contest.getId()));
		}
	}

	private List<ElectionOption> getOrderedElectionOptionsFromOptionsTemplateContest(final List<ElectionAttributes> attributes,
			final List<ElectionOption> electionOptions) {

		return attributes.stream()
				.filter(ElectionAttributes::isCorrectness)
				.map(ElectionAttributes::getId)
				.map(correctnessId -> getOrderedElectionOptions(correctnessId, attributes, electionOptions))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private List<ElectionOption> getOrderedElectionOptionsFromListsAndCandidatesTemplateContest(final List<Question> questions,
			final List<ElectionAttributes> attributes, final List<ElectionOption> electionOptions, final String contestId) {

		checkQuestionsSizeOfListsAndCandidatesContest(contestId, questions);

		if (Contest.isSwappingOfQuestionsNeeded(questions, attributes)) {
			Collections.swap(questions, 0, 1);
		}

		return questions.stream()
				.map(Question::getAttribute)
				.map(correctnessId -> getOrderedElectionOptions(correctnessId, attributes, electionOptions))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the ordered list of {@link ElectionOption}s given the {@code correctnessId}, the list of {@link ElectionAttributes} and the list of
	 * {@link ElectionOption}s of the {@link Contest}. We order the election options by their relative order in the {@link ElectionAttributes} list.
	 * The ordered list of {@link ElectionOption}s is computed as follows:
	 * <ul>
	 *     <li>List the related election attributes ids, ie list of election attributes ids whose field related contains the input value correctnessId.</li>
	 *     <li>List the options that have the corresponding election attributes id.</li>
	 * </ul>
	 *
	 * @param correctnessId   the correctness id to filter the related election attributes ids. Must be non-null.
	 * @param attributes      the list of {@link ElectionAttributes} of the {@link Contest}. Must be non-null.
	 * @param electionOptions the list {@link ElectionOption} of the {@link Contest}. Must be non-null.
	 * @return the ordered list of {@link ElectionOption}s.
	 * @throws NullPointerException if any of the inputs is null.
	 */
	private static List<ElectionOption> getOrderedElectionOptions(final String correctnessId, final List<ElectionAttributes> attributes,
			final List<ElectionOption> electionOptions) {
		checkNotNull(correctnessId);
		checkNotNull(attributes);
		checkNotNull(electionOptions);

		return attributes.stream()
				.filter(ea -> ea.getRelated() != null && ea.getRelated().contains(correctnessId))
				.map(ElectionAttributes::getId)
				.map(electionAttributesId -> electionOptions.stream()
						.filter(electionOption -> electionAttributesId.equals(electionOption.getAttribute()))
						.collect(Collectors.toList()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
}
