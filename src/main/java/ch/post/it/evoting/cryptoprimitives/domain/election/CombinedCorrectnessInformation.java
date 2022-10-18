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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.utils.Conversions;

public class CombinedCorrectnessInformation implements HashableList {

	@JsonProperty
	private final List<CorrectnessInformation> correctnessInformationList;

	// Corresponds to the variable 𝜓 - the number of voting options a voter can select.
	private int totalNumberOfSelections;

	// Corresponds to the variable n - the number of possible voting options.
	private int totalNumberOfVotingOptions;

	private List<BigInteger> totalListOfWriteInOptions;
	private int totalNumberOfWriteInOptions;

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
	 * Gets the correctness id corresponding to the provided selection index.
	 *
	 * @param index the selection index to get the corresponding correctness id. Must respect
	 *              {@code 0 <= index < number of voting options a voter can select}.
	 * @return the corresponding correctness id.
	 * @throws IllegalArgumentException if the provided selection index does not respect its preconditions or does not correspond to any correctness
	 *                                  id.
	 */
	public String getCorrectnessIdForSelectionIndex(final int index) {
		checkArgument(index >= 0, String.format("The provided index %s is negative.", index));
		checkArgument(index < this.totalNumberOfSelections, String.format("There are less correctnessIds than the provided index %s.", index));

		return this.correctnessIdToListOfSelectionsIndexesMap.entrySet().stream().filter(entry -> entry.getValue().contains(index))
				.map(Map.Entry::getKey).findAny()
				.orElseThrow(() -> new IllegalArgumentException(String.format("The provided index %s could not be found.", index)));
	}

	/**
	 * Gets the correctness id corresponding to the provided voting option index.
	 *
	 * @param index the voting option index to get the corresponding correctness id. Must respect
	 *              {@code 0 <= index < number of possible voting options}.
	 * @return the corresponding correctness id.
	 * @throws IllegalArgumentException if the provided voting option index does not respect its preconditions or does not correspond to any
	 *                                  correctness id.
	 */
	public String getCorrectnessIdForVotingOptionIndex(final int index) {
		checkArgument(index >= 0, String.format("The provided index %s is negative.", index));
		checkArgument(index < this.totalNumberOfVotingOptions, String.format("There are less voting options than the provided index %s.", index));

		return this.correctnessIdToListOfVotingOptionsIndexesMap.entrySet().stream().filter(entry -> entry.getValue().contains(index))
				.map(Map.Entry::getKey).findAny()
				.orElseThrow(() -> new IllegalArgumentException(String.format("The provided index %s could not be found.", index)));
	}

	/**
	 * @return the variable 𝜓 - the number of voting options a voter can select.
	 */
	@JsonIgnore
	public int getTotalNumberOfSelections() {
		return this.totalNumberOfSelections;
	}

	/**
	 * @return the variable n - the number of possible voting options.
	 */
	@JsonIgnore
	public int getTotalNumberOfVotingOptions() {
		return this.totalNumberOfVotingOptions;
	}

	/**
	 * @return the total list of writeInOptions.
	 */
	@JsonIgnore
	public List<BigInteger> getTotalListOfWriteInOptions() {
		return this.totalListOfWriteInOptions;
	}

	/**
	 * @return the total number of writeInOptions.
	 */
	@JsonIgnore
	public int getTotalNumberOfWriteInOptions() {
		return this.totalNumberOfWriteInOptions;
	}

	public List<CorrectnessInformation> getCorrectnessInformationList() {
		return this.correctnessInformationList;
	}

	private static List<CorrectnessInformation> getCorrectnessInformationListFromBallot(final Ballot ballot) {

		return checkContestsNotNullAndNotEmpty(ballot.contests(), ballot.id()).stream()
				.map(CombinedCorrectnessInformation::getCorrectnessInformationListFromContest)
				.flatMap(Collection::stream)
				.toList();
	}

	/**
	 * Retrieves the list of {@link CorrectnessInformation} related to the given {@link Contest}, ensuring that the order of the {@link Question}s is
	 * the same as they appear on the voter portal.
	 * <p>
	 * Matching the order of {@link Question}s, two different business logics must be applied:
	 * <ul>
	 *   <li>For a contest with template {@value Contest#OPTIONS_TEMPLATE} (containing referendum-style questions), the order
	 *   of {@link Question}s is determined by the {@link ElectionAttributes} of the {@link Contest}.</li>
	 *   <li>For a contest with template {@value Contest#LISTS_AND_CANDIDATES_TEMPLATE}, the order of {@link Question}s is
	 *   the same as they appear on the voter portal as long as we ensure that the "lists" question comes before the
	 *   {@value Contest#CANDIDATES} question.</li>
	 * </ul>
	 *
	 * @param contest the contest.
	 * @return the correctness information list.
	 * @throws IllegalArgumentException if the given contest's template is unsupported. Supported templates are
	 *                                  <ul>
	 *                                      <li>{@value Contest#LISTS_AND_CANDIDATES_TEMPLATE}</li>
	 *                                      <li>{@value Contest#OPTIONS_TEMPLATE}</li>
	 *                                  </ul>
	 */
	private static List<CorrectnessInformation> getCorrectnessInformationListFromContest(final Contest contest) {
		final String contestId = contest.id();
		final String template = contest.template();
		final List<Question> questions = contest.questions();
		final List<ElectionAttributes> attributes = contest.attributes();
		final List<ElectionOption> electionOptions = contest.options();

		checkNotNullAndNotEmpty(questions, "questions", contestId);
		checkNotNullAndNotEmpty(attributes, "election attributes", contestId);
		checkNotNullAndNotEmpty(electionOptions, "election options", contestId);

		if (Contest.OPTIONS_TEMPLATE.equals(template)) {
			return getCorrectnessInformationListFromOptionsTemplateContest(questions, attributes, electionOptions, contestId);

		} else if (Contest.LISTS_AND_CANDIDATES_TEMPLATE.equals(template)) {
			return getCorrectnessInformationListFromListsAndCandidatesTemplate(questions, attributes, electionOptions, contestId);

		} else {
			throw new IllegalArgumentException(
					String.format("Contests with template \"%s\" are not supported. [contestId: %s]", template, contestId));
		}
	}

	/**
	 * Retrieves the list of {@link CorrectnessInformation} related to the given {@code questions}, {@code attributes}, {@code options}, and
	 * {@code contestId} of a {@link Contest}.
	 * <p>
	 * In the case of a contest's template {@value Contest#OPTIONS_TEMPLATE}, the order of the correctness information list is the same as the order
	 * in which the attributes appear in the {@link Contest}.
	 *
	 * @param questions  the list of {@link Question}s of the {@link Contest}.
	 * @param attributes the list of {@link ElectionAttributes} of the {@link Contest}.
	 * @param options    the list of {@link ElectionOption} of the {@link Contest}.
	 * @param contestId  the id of the contest.
	 * @return the correctness information list.
	 * @throws IllegalArgumentException if any {@link ElectionAttributes} of the attributes list with correctness "true" does not have a corresponding
	 *                                  {@link Question} in the questions list.
	 */
	private static List<CorrectnessInformation> getCorrectnessInformationListFromOptionsTemplateContest(final List<Question> questions,
			final List<ElectionAttributes> attributes, final List<ElectionOption> options, final String contestId) {

		return attributes.stream()
				.filter(ElectionAttributes::isCorrectness)
				.map(ElectionAttributes::getId)
				.map(correctnessId ->
						new CorrectnessInformation(correctnessId,
								getCorrespondingQuestionByAttribute(questions, correctnessId, contestId).max(),
								getNumberOfVotingOptions(attributes, options, correctnessId),
								Collections.emptyList()))
				.toList();
	}

	/**
	 * Retrieves the list of {@link CorrectnessInformation} related to the given {@code questions}, {@code attributes}, and {@code electionOptions} of
	 * a {@link Contest}.
	 * <p>
	 * In the case of a contest's template {@value Contest#LISTS_AND_CANDIDATES_TEMPLATE}, the order of the correctness information list must ensure
	 * that the "lists" question comes before the {@value Contest#CANDIDATES} question.
	 *
	 * @param questions       the list of {@link Question}s of the {@link Contest}. Size must be at most
	 *                        {@value Contest#MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE}.
	 * @param attributes      the list of {@link ElectionAttributes} of the {@link Contest}.
	 * @param electionOptions the list of {@link ElectionOption} of the {@link Contest}.
	 * @param contestId       the id of the contest.
	 * @return the correctness information list.
	 * @throws IllegalArgumentException if the questions list size is bigger than the maximal size of questions for contest's template
	 *                                  {@value Contest#LISTS_AND_CANDIDATES_TEMPLATE} {@value Contest#MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE}
	 */
	private static List<CorrectnessInformation> getCorrectnessInformationListFromListsAndCandidatesTemplate(final List<Question> questions,
			final List<ElectionAttributes> attributes, final List<ElectionOption> electionOptions, final String contestId) {

		checkQuestionsSizeOfListsAndCandidatesContest(contestId, questions);

		if (Contest.isSwappingOfQuestionsNeeded(questions, attributes)) {
			Collections.swap(questions, 0, 1);
		}

		return questions.stream()
				.map(question ->
						new CorrectnessInformation(question.attribute(),
								question.max(),
								getNumberOfVotingOptions(attributes, electionOptions, question.attribute()),
								getListOfWriteInOptions(question, electionOptions)))
				.toList();
	}

	/**
	 * Returns the list of prime numbers as {@link BigInteger}s that correspond to write-in options. We identify every write-in position with a
	 * distinct prime number. The number of elements of this list corresponds to the number of write-in positions for this particular election
	 * (Question). The method returns an empty list if no write-ins are allowed.
	 *
	 * @param question        a {@link Question} of the {@link Contest}.
	 * @param electionOptions the list {@link ElectionOption} of the {@link Contest}.
	 * @return the list of prime numbers as {@link BigInteger}s that correspond to write-in options.
	 */
	private static List<BigInteger> getListOfWriteInOptions(final Question question, final List<ElectionOption> electionOptions) {
		if (!question.isWriteIn()) {
			return Collections.emptyList();
		}

		final String writeInAttribute = question.writeInAttribute();

		return electionOptions.stream()
				.filter(electionOption -> electionOption.getAttribute().equals(writeInAttribute))
				.map(ElectionOption::getRepresentation)
				.map(Conversions::stringToInteger)
				.toList();
	}

	private void initCombinedCorrectnessInformation() {

		this.totalNumberOfSelections = computeTotalNumberOfSelections(this.correctnessInformationList);
		this.totalNumberOfVotingOptions = computeTotalNumberOfVotingOptions(this.correctnessInformationList);

		this.totalListOfWriteInOptions = computeTotalListOfWriteInOptions(this.correctnessInformationList);
		this.totalNumberOfWriteInOptions = computeTotalNumberOfWriteInOptions(this.correctnessInformationList);

		this.correctnessIdToListOfSelectionsIndexesMap = getCorrectnessIdToListOfIndexesMap(this.correctnessInformationList,
				CorrectnessInformation::numberOfSelections);
		this.correctnessIdToListOfVotingOptionsIndexesMap = getCorrectnessIdToListOfIndexesMap(this.correctnessInformationList,
				CorrectnessInformation::numberOfVotingOptions);
	}

	/**
	 * Computes the number of voting options for the given {@code correctnessId} within the given {@code attributes} and {@code electionOptions} of a
	 * contest.
	 * <p>
	 * For example, a correctnessId might identify a specific question. This question has three associated voting options (called election options in
	 * the Ballot model): YES, NO, EMPTY. Therefore, the method should return 3 in this case. Another example would be a correctnessId identifying the
	 * selection of a list in an election. Imagine that the voter can choose between 20 different lists. In this case, the method should return 21 (20
	 * lists + the blank list).
	 *
	 * @param attributes      the list of {@link ElectionAttributes} of the {@link Contest}.
	 * @param electionOptions the list {@link ElectionOption} of the {@link Contest}.
	 * @param correctnessId   the correctness id.
	 * @return the computed number of voting options.
	 */
	private static Integer getNumberOfVotingOptions(final List<ElectionAttributes> attributes, final List<ElectionOption> electionOptions,
			final String correctnessId) {

		// list of the related election attributes ids, ie list of election attributes ids whose field related (an array) contains the value correctnessId.
		final List<String> relatedElectionAttributesIdsList = attributes.stream()
				.filter(electionAttributes -> electionAttributes.getRelated() != null && electionAttributes.getRelated().contains(correctnessId))
				.map(ElectionAttributes::getId).toList();

		// the number of voting options is the number of election options which are present in the list of the related election attributes ids.
		final long numberOfVotingOptions = electionOptions.stream()
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
			final List<Integer> indexesList = IntStream.rangeClosed(currentIndex + 1, currentIndex + increment).boxed().toList();

			correctnessIdToListOfIndexesMap.put(correctnessInformation.correctnessId(), indexesList);

			currentIndex += increment;
		}

		return correctnessIdToListOfIndexesMap;
	}

	private static Question getCorrespondingQuestionByAttribute(final List<Question> questions, final String attribute, final String contestId) {
		return questions.stream().filter(question -> question.attribute().equals(attribute)).findAny().orElseThrow(
				() -> new IllegalArgumentException(
						String.format("No corresponding question to attribute found in contest. [contestId: %s, attributeId: %s].", contestId,
								attribute)));
	}

	private static int computeTotalNumberOfSelections(final List<CorrectnessInformation> correctnessInformationList) {
		return correctnessInformationList.stream()
				.map(CorrectnessInformation::numberOfSelections)
				.reduce(0, Integer::sum);
	}

	private static int computeTotalNumberOfVotingOptions(final List<CorrectnessInformation> correctnessInformationList) {
		return correctnessInformationList.stream()
				.map(CorrectnessInformation::numberOfVotingOptions)
				.reduce(0, Integer::sum);
	}

	private static List<BigInteger> computeTotalListOfWriteInOptions(final List<CorrectnessInformation> correctnessInformationList) {
		return correctnessInformationList.stream()
				.map(CorrectnessInformation::listOfWriteInOptions)
				.flatMap(Collection::stream)
				.toList();
	}

	private int computeTotalNumberOfWriteInOptions(final List<CorrectnessInformation> correctnessInformationList) {
		return correctnessInformationList.stream()
				.map(CorrectnessInformation::listOfWriteInOptions)
				.map(List::size)
				.reduce(0, Integer::sum);
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
