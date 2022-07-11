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
import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;
import java.util.List;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

public record VerificationCardSetContext(String verificationCardSetId,
										 String ballotBoxId,
										 boolean testBallotBox,
										 int numberOfWriteInFields,
										 int numberOfVotingCards,
										 int gracePeriod) implements HashableList {

	public VerificationCardSetContext {
		validateUUID(verificationCardSetId);
		validateUUID(ballotBoxId);
		checkArgument(numberOfWriteInFields >= 0, "The number of write-in fields must be positive.");
		checkArgument(numberOfVotingCards >= 0, "The number of voting cards must be positive.");
		checkArgument(gracePeriod >= 0, "The grace period must be positive.");
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(
				HashableString.from(verificationCardSetId),
				HashableString.from(ballotBoxId),
				HashableString.from(String.valueOf(testBallotBox)),
				HashableBigInteger.from(BigInteger.valueOf(numberOfWriteInFields)),
				HashableBigInteger.from(BigInteger.valueOf(numberOfVotingCards)),
				HashableBigInteger.from(BigInteger.valueOf(gracePeriod)));
	}
}
