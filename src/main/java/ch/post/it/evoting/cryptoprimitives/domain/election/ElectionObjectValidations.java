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

import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionAttributesAliasConstants.ALIAS_JOIN_DELIMITER;
import static ch.post.it.evoting.cryptoprimitives.domain.election.ElectionAttributesAliasConstants.ALIAS_SPLIT_REGEX;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.hasNoDuplicates;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateNonBlankUCS;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateXsToken;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

import ch.post.it.evoting.cryptoprimitives.domain.validations.Validations;

public final class ElectionObjectValidations {

	public static final int MINIMUM_GRACE_PERIOD = 1;
	public static final int MAXIMUM_GRACE_PERIOD = 3600;
	public static final int MINIMUM_NUMBER_OF_BOARD_MEMBERS = 1;
	public static final int MAXIMUM_NUMBER_OF_BOARD_MEMBERS = 10;

	private ElectionObjectValidations() {
		// Intentionally left blank.
	}

	public static String validateDefaultTitle(final String defaultTitle) {
		validateNonBlankUCS(defaultTitle);
		return defaultTitle;
	}

	public static String validateDefaultDescription(final String defaultDescription) {
		validateNonBlankUCS(defaultDescription);
		return defaultDescription;
	}

	public static String validateDefaultTitleWithPrefix(final String defaultTitle, final String prefix) {
		validateDefaultTitle(defaultTitle);
		checkNotNull(prefix);
		checkArgument(defaultTitle.startsWith(prefix),
				"The default title has the wrong prefix. [defaultTitle: %s, expectedPrefix: %s]", defaultTitle, prefix);
		return defaultTitle;
	}

	public static String validateDefaultDescriptionWithPrefix(final String defaultDescription, final String prefix) {
		validateDefaultDescription(defaultDescription);
		checkNotNull(prefix);
		checkArgument(defaultDescription.startsWith(prefix),
				"The default title has the wrong prefix. [defaultDescription: %s, expectedPrefix: %s]", defaultDescription, prefix);
		return defaultDescription;
	}

	public static void validateAlias(final String alias) {
		validateXsToken(alias);
	}

	public static void validateActualVotingOption(final String actualVotingOption) {
		checkNotNull(actualVotingOption);

		final String[] identifications = actualVotingOption.split(ALIAS_SPLIT_REGEX);
		checkArgument(identifications.length == 1 || identifications.length == 2,
				"The actual voting option should be either one identification or two identifications concatenated using %s.", ALIAS_JOIN_DELIMITER);
		Arrays.stream(identifications).forEach(Validations::validateXsToken);
	}

	public static void validateStatus(final String status) {
		validateXsToken(status);
	}

	public static void validateDateFromDateTo(final LocalDateTime dateFrom, final LocalDateTime dateTo) {
		checkNotNull(dateFrom);
		checkNotNull(dateTo);
		checkArgument(dateFrom.isBefore(dateTo),
				"The start date must be before the end date. [dateFrom: %s, dateTo:%s]", dateFrom, dateTo);
	}

	public static int validateGracePeriod(final int gracePeriod) {
		checkArgument(MINIMUM_GRACE_PERIOD <= gracePeriod && gracePeriod <= MAXIMUM_GRACE_PERIOD,
				"The grace period must be in the range [%s, %s]. [gracePeriod: %s]", MINIMUM_GRACE_PERIOD, MAXIMUM_GRACE_PERIOD, gracePeriod);
		return gracePeriod;
	}

	public static List<String> validateBoardMembers(final List<String> boardMembers) {
		checkNotNull(boardMembers);
		boardMembers.forEach(Preconditions::checkNotNull);
		checkArgument(hasNoDuplicates(boardMembers), "The board members must not contain duplicates.");
		checkArgument(MINIMUM_NUMBER_OF_BOARD_MEMBERS <= boardMembers.size() && boardMembers.size() <= MAXIMUM_NUMBER_OF_BOARD_MEMBERS,
				"There must be at least %s board members and at most %s board members. [numberOfBoardMembers: %s]",
				MINIMUM_NUMBER_OF_BOARD_MEMBERS, MAXIMUM_NUMBER_OF_BOARD_MEMBERS, boardMembers.size());
		return boardMembers;
	}
}
