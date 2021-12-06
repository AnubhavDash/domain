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
package ch.post.it.evoting.cryptoprimitives.domain.election;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.election.exceptions.CombinedCorrectnessInformationException;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;

public class CombinedCorrectnessInformation implements HashableList {

	private static final String LISTS_AND_CANDIDATES_TEMPLATE = "listsAndCandidates";
	private static final String CANDIDATES = "candidates";
	private static final int MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE = 2;

	@JsonProperty
	private final List<CorrectnessInformation> correctnessInformationList;

	// Corresponds to the variable 𝜓 - the number of voting options a voter can select.
	private Integer totalNumberOfSelections;

	// Corresponds to the variable n - the number of possible voting options.
	private Integer totalNumberOfVotingOptions;

	private Map<String, List<Integer>> correctnessIdToListOfSelectionsIndexesMap;
	private Map<String, List<Integer>> correctnessIdToListOfVotingOptionsIndexesMap;

	public CombinedCorrectnessInformation(final Ballot ballot) {
		checkNotNull(ballot, "The provided ballot is null.");

		this.correctnessInformationList = getCorrectnessInformationListFromBallot(ballot);

		initCombinedCorrectnessInformation();
	}

	@JsonCreator
	public CombinedCorrectnessInformation(
			@JsonProperty("correctnessInformationList")
			final List<CorrectnessInformation> correctnessInformationList) {
		checkNotNull(correctnessInformationList, "The provided correctnessInformationList is null.");

		this.correctnessInformationList = ImmutableList.copyOf(correctnessInformationList);

		initCombinedCorrectnessInformation();
	}

	/**
	 * Get the correctness id corresponding to the provided selection index.
	 *
	 * @param index the selection index to get the corresponding correctness id. Must respect 0 <= index < number of voting options a voter can
	 *              select.
	 * @return the corresponding correctness id.
	 * @throws IllegalArgumentException                if the provided selection index does not respect its preconditions.
	 * @throws CombinedCorrectnessInformationException if the provided selection index does not correspond to any correctness id.
	 */
	public String getCorrectnessIdForSelectionIndex(final int index) {
		checkArgument(index >= 0, String.format("The provided index %s is negative.", index));
		checkArgument(index < this.totalNumberOfSelections, String.format("There are less correctnessIds than the provided index %s.", index));

		return this.correctnessIdToListOfSelectionsIndexesMap.entrySet().stream().filter(entry -> entry.getValue().contains(index))
				.map(Map.Entry::getKey).findAny()
				.orElseThrow(() -> new CombinedCorrectnessInformationException(String.format("The provided index %s could not be found.", index)));
	}

	/**
	 * Get the correctness id corresponding to the provided voting option index.
	 *
	 * @param index the voting option index to get the corresponding correctness id. Must respect 0 <= index < number of possible voting options.
	 * @return the corresponding correctness id.
	 * @throws IllegalArgumentException                if the provided voting option index does not respect its preconditions.
	 * @throws CombinedCorrectnessInformationException if the provided voting option index does not correspond to any correctness id.
	 */
	public String getCorrectnessIdForVotingOptionIndex(final int index) {
		checkArgument(index >= 0, String.format("The provided index %s is negative.", index));
		checkArgument(index < this.totalNumberOfVotingOptions, String.format("There are less voting options than the provided index %s.", index));

		return this.correctnessIdToListOfVotingOptionsIndexesMap.entrySet().stream().filter(entry -> entry.getValue().contains(index))
				.map(Map.Entry::getKey).findAny()
				.orElseThrow(() -> new CombinedCorrectnessInformationException(String.format("The provided index %s could not be found.", index)));
	}

	/**
	 * @return the variable 𝜓 - the number of voting options a voter can select.
	 */
	@JsonIgnore
	public Integer getTotalNumberOfSelections() {
		return this.totalNumberOfSelections;
	}

	/**
	 * @return the variable n - the number of possible voting options.
	 */
	@JsonIgnore
	public Integer getTotalNumberOfVotingOptions() {
		return this.totalNumberOfVotingOptions;
	}

	public List<CorrectnessInformation> getCorrectnessInformationList() {
		return this.correctnessInformationList;
	}

	private static List<CorrectnessInformation> getCorrectnessInformationListFromBallot(final Ballot ballot) {
		checkContestsNotNullAndNotEmpty(ballot.getContests(), ballot.getId());

		final List<CorrectnessInformation> correctnessInformationList = new ArrayList<>();

		for (final Contest contest : ballot.getContests()) {

			final String contestId = contest.getId();
			final String template = contest.getTemplate();
			final List<Question> questions = contest.getQuestions();
			final List<ElectionAttributes> attributes = contest.getAttributes();

			checkNotNullAndNotEmpty(questions, "questions", contestId);
			checkQuestionsSizeOfContest(contestId, template, questions);

			checkNotNullAndNotEmpty(attributes, "election attributes", contestId);
			checkNotNullAndNotEmpty(contest.getOptions(), "election options", contestId);

			if (isSwappingOfQuestionsNeeded(template, questions, attributes)) {
				Collections.swap(questions, 0, 1);
			}

			for (final Question question : questions) {

				final String correctnessId = question.getAttribute();
				final Integer numberOfVotingOptions = getNumberOfVotingOptions(contest, correctnessId);
				final Integer maximalSelections = question.getMax();

				correctnessInformationList.add(new CorrectnessInformation(correctnessId, maximalSelections, numberOfVotingOptions));
			}
		}

		return correctnessInformationList;

	}

	private void initCombinedCorrectnessInformation() {

		this.totalNumberOfSelections = computeTotalNumberOfSelections(this.correctnessInformationList);
		this.totalNumberOfVotingOptions = computeTotalNumberOfVotingOptions(this.correctnessInformationList);

		this.correctnessIdToListOfSelectionsIndexesMap = getCorrectnessIdToListOfIndexesMap(this.correctnessInformationList,
				CorrectnessInformation::getNumberOfSelections);
		this.correctnessIdToListOfVotingOptionsIndexesMap = getCorrectnessIdToListOfIndexesMap(this.correctnessInformationList,
				CorrectnessInformation::getNumberOfVotingOptions);
	}

	/**
	 * Computes the number of voting options for the given {@code correctnessId} within the given {@code contest}.
	 * <p>
	 * For example, a correctnessId might identify a specific question. This question has three associated voting options (called election options in
	 * the Ballot model): YES, NO, EMPTY. Therefore, the method should return 3 in this case. Another example would be a correctnessId identifying the
	 * selection of a list in an election. Imagine that the voter can choose between 20 different lists. In this case, the method should return 21 (20
	 * lists + the blank list).
	 *
	 * @param contest       the contest.
	 * @param correctnessId the correctness id.
	 * @return the computed number of voting options.
	 */
	private static Integer getNumberOfVotingOptions(final Contest contest, final String correctnessId) {

		// list of the related election attributes ids, ie list of election attributes ids whose field related (an array) contains the value correctnessId.
		final List<String> relatedElectionAttributesIdsList = contest.getAttributes().stream()
				.filter(electionAttributes -> electionAttributes.getRelated() != null && electionAttributes.getRelated().contains(correctnessId))
				.map(ElectionAttributes::getId).collect(Collectors.toList());

		// the number of voting options is the number of election options which are present in the list of the related election attributes ids.
		final long numberOfVotingOptions = contest.getOptions().stream()
				.filter(electionOption -> relatedElectionAttributesIdsList.contains(electionOption.getAttribute())).count();

		return Math.toIntExact(numberOfVotingOptions);
	}

	/**
	 * Returns a map based on the given {@code correctnessInformationList} and the given {@code getIncrementFunction}.
	 * <p>
	 * For each correctness information entry in the list, we add an entry with :
	 * <ul>
	 *     <li>key: the correctness information's correctness id field.</li>
	 *     <li>value: a list of indexes going from [current index + 1] (inclusive) to [current index + increment] (inclusive).</li>
	 * </ul>
	 * When building the map, it iterates over the given {@code correctnessInformationList}, starting with a current index value of -1 and, after
	 * each map insertion, updating the current index to current index + increment.
	 * <p>
	 * Example for two correctness informations :
	 * <ul>
	 *     <li>Correctness information 1 : (correctnessId=1, increment=3)</li>
	 *     <li>Correctness information 2 : (correctnessId=2, increment=8)</li>
	 * </ul>
	 * Returned map would be :
	 * 	[
	 * 		(1, [0, 1, 2]),
	 * 		(2, [3, 4, 5, 6, 7, 8, 9, 10])
	 * 	].
	 *
	 * @param correctnessInformationList the list of correctness informations to process.
	 * @param getIncrementFunction       the function to get the increment for each correctness information.
	 * @return a new map based on the given {@code correctnessInformationList} and the given {@code getIncrementFunction}.
	 */
	private static Map<String, List<Integer>> getCorrectnessIdToListOfIndexesMap(final List<CorrectnessInformation> correctnessInformationList,
			final ToIntFunction<CorrectnessInformation> getIncrementFunction) {

		final Map<String, List<Integer>> correctnessIdToListOfIndexesMap = new HashMap<>();

		int currentIndex = -1;
		for (final CorrectnessInformation correctnessInformation : correctnessInformationList) {
			final int increment = getIncrementFunction.applyAsInt(correctnessInformation);
			final List<Integer> indexesList = IntStream.rangeClosed(currentIndex + 1, currentIndex + increment).boxed().collect(Collectors.toList());

			correctnessIdToListOfIndexesMap.put(correctnessInformation.getCorrectnessId(), indexesList);

			currentIndex += increment;
		}

		return correctnessIdToListOfIndexesMap;
	}

	private static Integer computeTotalNumberOfSelections(final List<CorrectnessInformation> correctnessInformationList) {
		return correctnessInformationList.stream().map(CorrectnessInformation::getNumberOfSelections).reduce(0, Integer::sum);
	}

	private static Integer computeTotalNumberOfVotingOptions(final List<CorrectnessInformation> correctnessInformationList) {
		return correctnessInformationList.stream().map(CorrectnessInformation::getNumberOfVotingOptions).reduce(0, Integer::sum);
	}

	/**
	 * The {@link Ballot} object orders the "{@link Question}s" (which can correspond to a selection of referendum-type questions, but also to a
	 * selection of a list or a number of candidates) according to the way the "questions" appear on the voter portal. However, in case of an election
	 * with lists and candidates (template {@value CombinedCorrectnessInformation#LISTS_AND_CANDIDATES_TEMPLATE} and 2 questions), if the first
	 * question corresponds to an election attribute with the alias {@value CombinedCorrectnessInformation#CANDIDATES}, we need to swap it with the
	 * second question. This swap ensures the first question relates to "lists" and the second to {@value CombinedCorrectnessInformation#CANDIDATES}.
	 *
	 * @param template   the template of the {@link Contest}.
	 * @param questions  the list of {@link Question}s of the {@link Contest}.
	 * @param attributes the list of {@link ElectionAttributes} of the {@link Contest}.
	 * @return true if the swapping is needed according to the conditions, false otherwise.
	 */
	private static boolean isSwappingOfQuestionsNeeded(final String template, final List<Question> questions,
			final List<ElectionAttributes> attributes) {

		if (LISTS_AND_CANDIDATES_TEMPLATE.equals(template) && questions.size() == 2) {
			final Question firstQuestion = questions.get(0);

			return attributes.stream().filter(ElectionAttributes::isCorrectness)
					.filter(electionAttributes -> CANDIDATES.equals(electionAttributes.getAlias()))
					.anyMatch(electionAttributes -> firstQuestion.getAttribute().equals(electionAttributes.getId()));
		}

		return false;
	}

	private static void checkQuestionsSizeOfContest(final String contestId, final String template, final List<Question> questions) {
		if (LISTS_AND_CANDIDATES_TEMPLATE.equals(template) && questions.size() > MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE) {
			throw new CombinedCorrectnessInformationException(
					String.format("A contest with template \"%s\" cannot have more than %s questions. [contestId=%s, questions size of contest=%s]",
							LISTS_AND_CANDIDATES_TEMPLATE, MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE, contestId, questions.size()));
		}
	}

	private static void checkContestsNotNullAndNotEmpty(final List<Contest> contests, final String ballotId) {
		if (contests == null) {
			throw new CombinedCorrectnessInformationException(String.format("The provided contests for the ballot with id %s are null.", ballotId));
		} else if (contests.isEmpty()) {
			throw new CombinedCorrectnessInformationException(String.format("The provided contests for the ballot with id %s are empty.", ballotId));
		}
	}

	private static void checkNotNullAndNotEmpty(final List<?> parameterList, final String parameterListContentDescription, final String contestId) {
		if (parameterList == null) {
			throw new CombinedCorrectnessInformationException(
					String.format("The provided %s for the contest with id %s are null.", parameterListContentDescription, contestId));
		} else if (parameterList.isEmpty()) {
			throw new CombinedCorrectnessInformationException(
					String.format("The provided %s for the contest with id %s are empty.", parameterListContentDescription, contestId));
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CombinedCorrectnessInformation that = (CombinedCorrectnessInformation) o;
		return correctnessInformationList.equals(that.correctnessInformationList) && Objects.equals(totalNumberOfSelections,
				that.totalNumberOfSelections) && Objects.equals(totalNumberOfVotingOptions, that.totalNumberOfVotingOptions) && Objects.equals(
				correctnessIdToListOfSelectionsIndexesMap, that.correctnessIdToListOfSelectionsIndexesMap) && Objects.equals(
				correctnessIdToListOfVotingOptionsIndexesMap, that.correctnessIdToListOfVotingOptionsIndexesMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(correctnessInformationList, totalNumberOfSelections, totalNumberOfVotingOptions,
				correctnessIdToListOfSelectionsIndexesMap, correctnessIdToListOfVotingOptionsIndexesMap);
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return ImmutableList.copyOf(correctnessInformationList);
	}

}
