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

import static ch.post.it.evoting.cryptoprimitives.domain.ControlComponentConstants.NODE_IDS;
import static ch.post.it.evoting.cryptoprimitives.domain.validations.Validations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamal;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalFactory;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

public record ElectionEventContext(String electionEventId,
								   List<VerificationCardSetContext> verificationCardSetContexts,
								   List<ControlComponentPublicKeys> combinedControlComponentPublicKeys,
								   ElGamalMultiRecipientPublicKey electoralBoardPublicKey,
								   ElGamalMultiRecipientPublicKey electionPublicKey,
								   ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey,
								   LocalDateTime startTime,
								   LocalDateTime finishTime) implements HashableList {

	public ElectionEventContext(final String electionEventId,
			final List<VerificationCardSetContext> verificationCardSetContexts,
			final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys,
			final ElGamalMultiRecipientPublicKey electoralBoardPublicKey,
			final ElGamalMultiRecipientPublicKey electionPublicKey,
			final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey,
			final LocalDateTime startTime,
			final LocalDateTime finishTime) {

		this.electionEventId = validateUUID(electionEventId);
		this.verificationCardSetContexts = List.copyOf(checkNotNull(verificationCardSetContexts));
		this.combinedControlComponentPublicKeys = List.copyOf(checkNotNull(combinedControlComponentPublicKeys));
		this.electoralBoardPublicKey = checkNotNull(electoralBoardPublicKey);
		this.electionPublicKey = checkNotNull(electionPublicKey);
		this.choiceReturnCodesEncryptionPublicKey = checkNotNull(choiceReturnCodesEncryptionPublicKey);
		this.startTime = checkNotNull(startTime);
		this.finishTime = checkNotNull(finishTime);

		this.verificationCardSetContexts.forEach(Preconditions::checkNotNull);
		this.combinedControlComponentPublicKeys.forEach(Preconditions::checkNotNull);

		final int verificationCardSetContextsSize = this.verificationCardSetContexts.size();
		checkArgument(verificationCardSetContextsSize > 0, "VerificationCardSetContexts cannot be empty.");
		checkArgument(this.verificationCardSetContexts.stream()
				.map(VerificationCardSetContext::ballotBoxId)
				.distinct()
				.count() == verificationCardSetContextsSize, "VerificationCardSetContexts cannot contain duplicate BallotBoxIds.");
		checkArgument(this.verificationCardSetContexts.stream()
				.map(VerificationCardSetContext::verificationCardSetId)
				.distinct()
				.count() == verificationCardSetContextsSize, "VerificationCardSetContexts cannot contain duplicate VerificationCardSetIds.");
		checkArgument(this.verificationCardSetContexts.stream()
						.map(VerificationCardSetContext::numberOfWriteInFields)
						.allMatch(n -> n >= 0),
				"VerificationCardSetContexts cannot contain negative numberOfWriteInFields.");

		final int combinedControlComponentPublicKeysSize = this.combinedControlComponentPublicKeys.size();
		checkArgument(combinedControlComponentPublicKeysSize == NODE_IDS.size(),
				"CombinedControlComponentPublicKeys must contain the expected number of ControlComponentPublicKeys.");
		checkArgument(NODE_IDS.equals(
						this.combinedControlComponentPublicKeys.stream()
								.map(ControlComponentPublicKeys::nodeId)
								.collect(Collectors.toSet())),
				"CombinedControlComponentPublicKeys must contain the expected node ids.");

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrChoiceReturnCodePublicKeys = this.combinedControlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::ccrChoiceReturnCodesEncryptionPublicKey)
				.collect(GroupVector.toGroupVector());

		final ElGamal elGamal = ElGamalFactory.createElGamal();
		final ElGamalMultiRecipientPublicKey combinedCCrChoiceReturnCodesPublicKeys = elGamal.combinePublicKeys(ccrChoiceReturnCodePublicKeys);

		checkArgument(choiceReturnCodesEncryptionPublicKey.equals(combinedCCrChoiceReturnCodesPublicKeys),
				"Multiplication of the ccrChoiceReturnCodesPublicKeys must equal the choiceReturnCodesPublicKey");

		final int maxNumberOfWriteInFields = getMaxNumberOfWriteInFields();

		checkArgument(electoralBoardPublicKey.size() == (maxNumberOfWriteInFields + 1),
				"The size of the electoralBoardPublicKey must equal the maximum number of write-in fields in all verification card sets + 1");

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> publicKeys = Streams.concat(
						this.combinedControlComponentPublicKeys.stream()
								.map(ControlComponentPublicKeys::ccmElectionPublicKey)
								.filter(ccmElectionPublicKey -> ccmElectionPublicKey.size() >= maxNumberOfWriteInFields + 1)
								.map(ccmElectionPublicKey ->
										new ElGamalMultiRecipientPublicKey(
												ccmElectionPublicKey.getKeyElements().subList(0, maxNumberOfWriteInFields + 1))),
						Stream.of(electoralBoardPublicKey))
				.collect(GroupVector.toGroupVector());

		checkArgument(electionPublicKey.equals(elGamal.combinePublicKeys(publicKeys)),
				"Multiplication of the ccmElectionPublicKeys times the electoralBoardPublicKey must equal the electionPublicKey");

		final ControlComponentPublicKeys controlComponentPublicKey = this.combinedControlComponentPublicKeys.get(0);
		final GqGroup gqGroup = controlComponentPublicKey.ccmElectionPublicKey().getGroup();
		checkArgument(gqGroup.equals(electoralBoardPublicKey.getGroup()));
		checkArgument(gqGroup.equals(electionPublicKey.getGroup()));
		checkArgument(gqGroup.equals(choiceReturnCodesEncryptionPublicKey.getGroup()));
		checkArgument(gqGroup.equals(this.verificationCardSetContexts.get(0).primesMappingTable().getPTable().getGroup()));
	}

	/**
	 * @return the maximum number of write-in fields in all verification card sets.
	 */
	@JsonIgnore
	public int getMaxNumberOfWriteInFields() {
		return this.verificationCardSetContexts.stream()
				.max(Comparator.comparingInt(VerificationCardSetContext::numberOfWriteInFields))
				.orElseThrow(IllegalArgumentException::new) // Guaranteed to never occur by the constructor checkNotNull.
				.numberOfWriteInFields();
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(HashableString.from(electionEventId),
				HashableList.from(verificationCardSetContexts),
				HashableList.from(combinedControlComponentPublicKeys),
				electoralBoardPublicKey,
				electionPublicKey,
				choiceReturnCodesEncryptionPublicKey,
				HashableString.from(startTime.toString()),
				HashableString.from(finishTime.toString()));
	}
}