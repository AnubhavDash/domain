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

import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateXsToken;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Question(
		String id,
		Integer max,
		Integer min,
		Integer accumulation,
		boolean writeIn,
		String blankAttribute,
		String writeInAttribute,
		String attribute,
		List<List<String>> fusions,
		String questionNumber,
		boolean variantBallot,
		String ballotIdentification) {

	@JsonIgnore
	public static final String WRITE_IN_ATTRIBUTE_NO_WRITE_IN = String.valueOf(false);
	@JsonIgnore
	public static final int QUESTION_MAX_VALUE_LOWER_BOUND = 1;
	@JsonIgnore
	public static final int QUESTION_MAX_VALUE_UPPER_BOUND = 120;
	@JsonIgnore
	public static final int QUESTION_NUMBER_MAX_LENGTH = 15;

	public Question(final String id, final Integer max, final Integer min, final Integer accumulation, final boolean writeIn,
			final String blankAttribute, final String writeInAttribute, final String attribute, final List<List<String>> fusions,
			final String questionNumber, final boolean variantBallot, final String ballotIdentification) {
		this.id = validateUUID(id);
		this.max = checkNotNull(max);
		this.min = checkNotNull(min);
		this.accumulation = checkNotNull(accumulation);
		this.writeIn = writeIn;
		this.blankAttribute = validateUUID(blankAttribute);
		this.writeInAttribute = checkNotNull(writeInAttribute);
		this.attribute = validateUUID(attribute);
		this.fusions = checkNotNull(fusions).stream().map(Preconditions::checkNotNull).map(List::copyOf).toList();
		this.questionNumber = validateXsToken(questionNumber);
		this.variantBallot = variantBallot;
		this.ballotIdentification = validateXsToken(ballotIdentification);

		if (!writeInAttribute.equals(WRITE_IN_ATTRIBUTE_NO_WRITE_IN)) {
			validateUUID(writeInAttribute);
		}

		checkArgument(min.compareTo(0) == 0 || min.compareTo(1) == 0, "The min must be either 0 or 1. [min: %s, questionId: %s]", min, id);
		checkArgument(max.compareTo(QUESTION_MAX_VALUE_LOWER_BOUND) >= 0 && max.compareTo(QUESTION_MAX_VALUE_UPPER_BOUND) <= 0,
				"The max must be in the range [%s, %s]. [max: %s, questionId: %s]", QUESTION_MAX_VALUE_LOWER_BOUND, QUESTION_MAX_VALUE_UPPER_BOUND,
				max,
				id);
		checkArgument(min.compareTo(max) <= 0, "The min must be smaller or equal to the max. [min: %s, max: %s, questionId: %s]", min, max, id);

		checkArgument(accumulation.compareTo(0) > 0 && accumulation.compareTo(4) < 0,
				"The accumulation must be either 1, 2 or 3. [accumulation: %s, questionId: %s]", accumulation, id);

		checkArgument(questionNumber.length() <= QUESTION_NUMBER_MAX_LENGTH,
				"The question number must contain at most %s characters. [questionId: %s]", QUESTION_NUMBER_MAX_LENGTH, id);
	}

	@JsonGetter("max")
	public String getMax() {
		return String.valueOf(max);
	}

	@JsonGetter("min")
	public String getMin() {
		return String.valueOf(min);
	}

	@JsonGetter("accumulation")
	public String getAccumulation() {
		return String.valueOf(accumulation);
	}

	@JsonGetter("writeIn")
	public String getWriteIn() {
		return String.valueOf(writeIn);
	}

	@Override
	public List<List<String>> fusions() {
		return fusions.stream().map(List::copyOf).toList();
	}

	@JsonIgnore
	public boolean isWriteIn() {
		return writeIn;
	}
}
