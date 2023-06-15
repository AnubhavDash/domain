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

import static ch.post.it.evoting.cryptoprimitives.domain.election.BallotValidations.checkNotNullAndNotEmpty;
import static ch.post.it.evoting.cryptoprimitives.domain.election.BallotValidations.checkQuestionsSizeOfListsAndCandidatesContest;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateAlias;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultDescriptionWithPrefix;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateDefaultTitleWithPrefix;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionObjectValidations.validateStatus;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.hasNoDuplicates;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.MoreCollectors;

import ch.post.it.evoting.cryptoprimitives.utils.Conversions;

/**
 * Encapsulates the information contained within a ballot.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Ballot(String id,
					 String defaultTitle,
					 String defaultDescription,
					 String alias,
					 String status,
					 Identifier electionEvent,
					 List<Contest> contests) {

	@JsonIgnore
	public static final String PREFIX = "ballot_";
	@JsonIgnore
	public static final int MINIMUM_NUMBER_OF_CONTESTS = 1;
	@JsonIgnore
	public static final int MAXIMUM_NUMBER_OF_CONTESTS = 50;

	public Ballot {
		validateUUID(id);
		validateDefaultTitleWithPrefix(defaultTitle, PREFIX);
		validateDefaultDescriptionWithPrefix(defaultDescription, PREFIX);
		validateAlias(alias);
		validateStatus(status);
		checkNotNull(electionEvent);
		checkNotNull(contests);
		contests.forEach(Preconditions::checkNotNull);

		checkArgument(!contests.isEmpty(), "The ballot must contain at least one contest. [ballotId: %s]", id);
		checkArgument(hasNoDuplicates(contests.stream().map(Contest::id).toList()),
				"The ballot must not contain any duplicate contest id. [ballotId: %s]", id);
		checkArgument(hasNoDuplicates(contests.stream().map(Contest::alias).toList()),
				"The ballot must not contain any duplicate contest alias. [ballotId: %s]", id);
		checkArgument(MINIMUM_NUMBER_OF_CONTESTS <= contests.size() && contests.size() <= MAXIMUM_NUMBER_OF_CONTESTS,
				"There must be at least %s contests and at most %s contests. [numberOfContests: %s, ballotId: %s]", MINIMUM_NUMBER_OF_CONTESTS,
				MAXIMUM_NUMBER_OF_CONTESTS, contests.size(), id);
	}

	/**
	 * Returns the voting options - encoded as prime numbers - for this ballot. We order the encoded voting options by two levels: First, by how the
	 * corresponding questions are displayed on the voter portal and second, by how the voting options appear in the {@link ElectionAttributes} object
	 * of each contest.
	 *
	 * @return the voting options encoded as prime numbers.
	 * @throws IllegalArgumentException if the ballot contains an unsupported {@link Contest} template. Supported templates are
	 *                                  <ul>
	 *                                      <li>{@value Contest#ELECTIONS_TEMPLATE}</li>
	 *                                      <li>{@value Contest#VOTES_TEMPLATE}</li>
	 *                                  </ul>
	 * @throws ArithmeticException      if an encoded voting option is too big to be encoded as an Integer.
	 */
	@JsonIgnore
	public List<Integer> getEncodedVotingOptions() {
		return getOrderedElectionOptions().stream()
				.map(electionOption -> Conversions.stringToInteger(electionOption.getRepresentation()))
				.map(BigInteger::intValueExact)
				.toList();
	}

	/**
	 * Returns the actual voting options - the identifiers of the voting options - for this ballot.  We order the identifiers of the voting options by
	 * two levels: First, by how the corresponding questions are displayed on the voter portal and second, by how the voting options appear in the
	 * {@link ElectionAttributes} object of each contest.
	 *
	 * @return the actual voting options.
	 * @throws IllegalArgumentException if the ballot contains an unsupported {@link Contest} template. Supported templates are
	 *                                  <ul>
	 *                                      <li>{@value Contest#ELECTIONS_TEMPLATE}</li>
	 *                                      <li>{@value Contest#VOTES_TEMPLATE}</li>
	 *                                  </ul>
	 */
	@JsonIgnore
	public List<String> getActualVotingOptions() {
		final Map<Contest, List<ElectionOption>> contestToOrderedElectionOptions = this.contests.stream()
				.collect(Collectors.toMap(Function.identity(), this::getOrderedElectionOptionsFromContest));

		return contests.stream()
				.map(contest -> contestToOrderedElectionOptions.get(contest).stream()
						.map(electionOption -> getAttributeAlias(electionOption.getAttribute(), contest.attributes()))
						.toList()
				).flatMap(Collection::stream)
				.toList();
	}

	/**
	 * Returns the semantic information for this ballot. We order the semantic information by two levels: First, by how the corresponding questions
	 * are displayed on the voter portal and second, by how the voting options appear in the {@link ElectionAttributes} object of each contest.
	 *
	 * @return the semantic information.
	 * @throws IllegalArgumentException if the ballot contains an unsupported {@link Contest} template. Supported templates are
	 *                                  <ul>
	 *                                      <li>{@value Contest#ELECTIONS_TEMPLATE}</li>
	 *                                      <li>{@value Contest#VOTES_TEMPLATE}</li>
	 *                                  </ul>
	 */
	@JsonIgnore
	public List<String> getSemanticInformation() {
		return getOrderedElectionOptions().stream()
				.map(ElectionOption::getSemantics)
				.toList();
	}

	/**
	 * Returns the ordered {@link ElectionOption}s for this ballot. We order the election options by two levels: First, by how the corresponding
	 * questions of each contest are displayed on the voter portal and second, by how the voting options appear in the {@link ElectionAttributes}
	 * object of each contest.
	 *
	 * @throws IllegalArgumentException if the ballot contains an unsupported {@link Contest} template. Supported templates are
	 *                                  <ul>
	 *                                      <li>{@value Contest#ELECTIONS_TEMPLATE}</li>
	 *                                      <li>{@value Contest#VOTES_TEMPLATE}</li>
	 *                                  </ul>
	 */
	@JsonIgnore
	public List<ElectionOption> getOrderedElectionOptions() {

		return this.contests.stream()
				.map(this::getOrderedElectionOptionsFromContest)
				.flatMap(Collection::stream)
				.toList();
	}

	private List<ElectionOption> getOrderedElectionOptionsFromContest(final Contest contest) {
		final String contestId = contest.id();
		final String template = contest.template();
		final List<Question> questions = contest.questions();
		final List<ElectionAttributes> electionAttributes = contest.attributes();
		final List<ElectionOption> electionOptions = contest.options();

		checkNotNullAndNotEmpty(questions, "questions", contestId);
		checkNotNullAndNotEmpty(electionAttributes, "election attributes", contestId);
		checkNotNullAndNotEmpty(electionOptions, "election options", contestId);

		return switch (template) {
			case Contest.VOTES_TEMPLATE -> getOrderedElectionOptionsFromOptionsTemplateContest(electionAttributes, electionOptions);
			case Contest.ELECTIONS_TEMPLATE ->
					getOrderedElectionOptionsFromListsAndCandidatesTemplateContest(questions, electionAttributes, electionOptions, contestId);
			default -> throw new IllegalArgumentException(
					String.format("Contests with template \"%s\" are not supported. [contestId: %s]", template, contest.id()));
		};
	}

	private List<ElectionOption> getOrderedElectionOptionsFromOptionsTemplateContest(final List<ElectionAttributes> electionAttributes,
			final List<ElectionOption> electionOptions) {

		return electionAttributes.stream()
				.filter(ElectionAttributes::isCorrectness)
				.map(ElectionAttributes::getId)
				.map(correctnessId -> getOrderedElectionOptions(correctnessId, electionAttributes, electionOptions))
				.flatMap(Collection::stream)
				.toList();
	}

	private List<ElectionOption> getOrderedElectionOptionsFromListsAndCandidatesTemplateContest(final List<Question> questions,
			final List<ElectionAttributes> electionAttributes, final List<ElectionOption> electionOptions, final String contestId) {

		checkQuestionsSizeOfListsAndCandidatesContest(contestId, questions);

		if (Contest.isSwappingOfQuestionsNeeded(questions, electionAttributes)) {
			Collections.swap(questions, 0, 1);
		}

		return questions.stream()
				.map(Question::attribute)
				.map(correctnessId -> getOrderedElectionOptions(correctnessId, electionAttributes, electionOptions))
				.flatMap(Collection::stream)
				.toList();
	}

	/**
	 * Returns the attribute's alias (which corresponds to the identifier of the actual voting option) for a given attribute id
	 *
	 * @param attributeId        the attribute id.
	 * @param electionAttributes the attributes list to look into.
	 */
	@VisibleForTesting
	static String getAttributeAlias(final String attributeId, final List<ElectionAttributes> electionAttributes) {

		return electionAttributes.stream()
				.filter(electionAttribute -> attributeId.equals(electionAttribute.getId()))
				.map(ElectionAttributes::getAlias)
				.collect(MoreCollectors.onlyElement());
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
	 * @param correctnessId      the correctness id to filter the related election attributes ids. Must be non-null.
	 * @param electionAttributes the list of {@link ElectionAttributes} of the {@link Contest}. Must be non-null.
	 * @param electionOptions    the list {@link ElectionOption} of the {@link Contest}. Must be non-null.
	 * @return the ordered list of {@link ElectionOption}s.
	 * @throws NullPointerException if any of the inputs is null.
	 */
	private static List<ElectionOption> getOrderedElectionOptions(final String correctnessId, final List<ElectionAttributes> electionAttributes,
			final List<ElectionOption> electionOptions) {
		checkNotNull(correctnessId);
		checkNotNull(electionAttributes);
		checkNotNull(electionOptions);

		return electionAttributes.stream()
				.filter(electionAttribute -> electionAttribute.getRelated() != null && electionAttribute.getRelated().contains(correctnessId))
				.map(ElectionAttributes::getId)
				.map(electionAttributesId -> electionOptions.stream()
						.filter(electionOption -> electionAttributesId.equals(electionOption.getAttribute()))
						.toList())
				.flatMap(Collection::stream)
				.toList();
	}

	public static final class BallotBuilder {
		private String id;
		private String defaultTitle;
		private String defaultDescription;
		private String alias;
		private String status;
		private Identifier electionEvent;
		private List<Contest> contests;

		public BallotBuilder setId(final String id) {
			this.id = id;
			return this;
		}

		public BallotBuilder setDefaultTitle(final String defaultTitle) {
			this.defaultTitle = defaultTitle;
			return this;
		}

		public BallotBuilder setDefaultDescription(final String defaultDescription) {
			this.defaultDescription = defaultDescription;
			return this;
		}

		public BallotBuilder setAlias(final String alias) {
			this.alias = alias;
			return this;
		}

		public BallotBuilder setStatus(final String status) {
			this.status = status;
			return this;
		}

		public BallotBuilder setElectionEvent(final Identifier electionEvent) {
			this.electionEvent = electionEvent;
			return this;
		}

		public BallotBuilder setContests(final List<Contest> contests) {
			this.contests = contests;
			return this;
		}

		public Ballot build() {
			return new Ballot(id, defaultTitle, defaultDescription, alias, status, electionEvent, contests);
		}
	}
}
