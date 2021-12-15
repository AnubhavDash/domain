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

import java.util.List;

public final class BallotValidations {

	private BallotValidations() {
		// Intentionally left blank.
	}

	public static void checkQuestionsSizeOfListsAndCandidatesContest(final String contestId, final List<Question> questions) {
		if (questions.size() > Contest.MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE) {
			throw new IllegalArgumentException(
					String.format("A contest with template \"%s\" cannot have more than %s questions. [contestId=%s, questions size of contest=%s]",
							Contest.LISTS_AND_CANDIDATES_TEMPLATE, Contest.MAX_LISTS_AND_CANDIDATES_QUESTIONS_SIZE, contestId, questions.size()));
		}
	}

	public static List<Contest> checkContestsNotNullAndNotEmpty(final List<Contest> contests, final String ballotId) {
		if (contests == null) {
			throw new IllegalArgumentException(String.format("The ballot contains a null contests list. [ballotId=%s]", ballotId));
		} else if (contests.isEmpty()) {
			throw new IllegalArgumentException(String.format("The ballot contains an empty contests list. [ballotId=%s]", ballotId));
		}

		return contests;
	}

	public static void checkNotNullAndNotEmpty(final List<?> parameterList, final String parameterListContentDescription, final String contestId) {
		if (parameterList == null) {
			throw new IllegalArgumentException(
					String.format("The contest contains a null %s list. [contestId=%s]", parameterListContentDescription, contestId));
		} else if (parameterList.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("The contest contains an empty %s list. [contestId=%s]", parameterListContentDescription, contestId));
		}
	}
}
