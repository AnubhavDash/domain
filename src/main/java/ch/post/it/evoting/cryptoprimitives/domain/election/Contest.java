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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates the information related to an election.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Contest(
		String id,
		String defaultTitle,
		String defaultDescription,
		String alias,
		String template,
		String fullBlank,
		List<ElectionOption> options,
		List<ElectionAttributes> attributes,
		List<Question> questions) {

	@JsonIgnore
	public static final String LISTS_AND_CANDIDATES_TEMPLATE = "listsAndCandidates";

	@JsonIgnore
	public static final String OPTIONS_TEMPLATE = "options";

	@JsonIgnore
	public static final String CANDIDATES = "candidates";

	@JsonIgnore
	public static final int MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE = 2;

	@JsonGetter("fullBlank")
	public boolean isFullBlank() {
		return Boolean.parseBoolean(fullBlank);
	}

	/**
	 * Indicates if a swapping of question is needed.
	 * <p>
	 * The {@link Ballot} object does not order the "{@link Question}s" (which can correspond to a selection of referendum-type questions, but also to
	 * a selection of a list or a number of candidates) according to the way the "questions" appear on the voter portal. In case of an election with
	 * lists and candidates (contest's template {@value Contest#LISTS_AND_CANDIDATES_TEMPLATE} and
	 * {@value Contest#MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE} questions), if the first question corresponds to an election attribute with the alias
	 * {@value Contest#CANDIDATES}, we need to swap it with the second question. This swap ensures the first question relates to "lists" and the
	 * second to {@value Contest#CANDIDATES}.
	 *
	 * @param questions  the list of {@link Question}s of the {@link Contest}. Must be non-null.
	 * @param attributes the list of {@link ElectionAttributes} of the {@link Contest}. Must be non-null.
	 * @return true if the swapping is needed according to the conditions, false otherwise.
	 * @throws NullPointerException if any of the inputs is null.
	 */
	@JsonIgnore
	public static boolean isSwappingOfQuestionsNeeded(final List<Question> questions, final List<ElectionAttributes> attributes) {
		checkNotNull(questions);
		checkNotNull(attributes);

		if (questions.size() == MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE) {
			final Question firstQuestion = questions.get(0);

			return attributes.stream()
					.filter(ElectionAttributes::isCorrectness)
					.filter(electionAttributes -> CANDIDATES.equals(electionAttributes.getAlias()))
					.anyMatch(electionAttributes -> firstQuestion.attribute().equals(electionAttributes.getId()));
		}

		return false;
	}
}
