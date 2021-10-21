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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Contains the ballot box information such as its id and the election event id.
 */
@JsonPropertyOrder({ "ballotBoxId", "electionEventId" })
public class BallotBoxDetails {

	@JsonProperty
	private final String ballotBoxId;

	@JsonProperty
	private final String electionEventId;

	@JsonCreator
	public BallotBoxDetails(
			@JsonProperty(value = "ballotBoxId", required = true)
			final String ballotBoxId,
			@JsonProperty(value = "electionEventId", required = true)
			final String electionEventId) {

		validateUUID(ballotBoxId);
		validateUUID(electionEventId);

		this.ballotBoxId = ballotBoxId;
		this.electionEventId = electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	@Override
	public String toString() {
		return String.format("%s-%s", ballotBoxId, electionEventId);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final BallotBoxDetails that = (BallotBoxDetails) o;
		return Objects.equals(ballotBoxId, that.ballotBoxId) && Objects.equals(electionEventId, that.electionEventId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ballotBoxId, electionEventId);
	}
}
