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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.SchnorrProofDeserializer;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamal;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalFactory;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.SchnorrProof;

public record SetupComponentPublicKeys(List<ControlComponentPublicKeys> combinedControlComponentPublicKeys,
									   ElGamalMultiRecipientPublicKey electoralBoardPublicKey,
									   @JsonDeserialize(using = SchnorrProofDeserializer.class)
									   GroupVector<SchnorrProof, ZqGroup> electoralBoardSchnorrProofs,
									   ElGamalMultiRecipientPublicKey electionPublicKey,
									   ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey) implements HashableList {

	public SetupComponentPublicKeys(final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys,
			final ElGamalMultiRecipientPublicKey electoralBoardPublicKey,
			@JsonDeserialize(using = SchnorrProofDeserializer.class)
			final GroupVector<SchnorrProof, ZqGroup> electoralBoardSchnorrProofs,
			final ElGamalMultiRecipientPublicKey electionPublicKey,
			final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey) {
		this.combinedControlComponentPublicKeys = List.copyOf(checkNotNull(combinedControlComponentPublicKeys));
		this.electoralBoardPublicKey = checkNotNull(electoralBoardPublicKey);
		this.electoralBoardSchnorrProofs = checkNotNull(electoralBoardSchnorrProofs);
		this.electionPublicKey = checkNotNull(electionPublicKey);
		this.choiceReturnCodesEncryptionPublicKey = checkNotNull(choiceReturnCodesEncryptionPublicKey);

		this.combinedControlComponentPublicKeys.forEach(Preconditions::checkNotNull);

		final int combinedControlComponentPublicKeysSize = combinedControlComponentPublicKeys.size();
		checkArgument(combinedControlComponentPublicKeysSize == NODE_IDS.size(),
				"CombinedControlComponentPublicKeys must contain the expected number of ControlComponentPublicKeys.");
		checkArgument(NODE_IDS.equals(
						combinedControlComponentPublicKeys.stream()
								.map(ControlComponentPublicKeys::nodeId)
								.collect(Collectors.toSet())),
				"CombinedControlComponentPublicKeys must contain the expected node ids.");

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrChoiceReturnCodePublicKeys = combinedControlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::ccrjChoiceReturnCodesEncryptionPublicKey)
				.collect(GroupVector.toGroupVector());

		final ElGamal elGamal = ElGamalFactory.createElGamal();
		final ElGamalMultiRecipientPublicKey combinedCCrChoiceReturnCodesPublicKeys = elGamal.combinePublicKeys(ccrChoiceReturnCodePublicKeys);

		checkArgument(choiceReturnCodesEncryptionPublicKey.equals(combinedCCrChoiceReturnCodesPublicKeys),
				"Multiplication of the ccrChoiceReturnCodesPublicKeys must equal the choiceReturnCodesPublicKey");

		final int maxNumberOfWriteInFields = electionPublicKey.size() - 1;

		checkArgument(electoralBoardPublicKey.size() == (maxNumberOfWriteInFields + 1),
				"The size of the electoralBoardPublicKey must equal the maximum number of write-in fields in all verification card sets + 1");
		checkArgument(electoralBoardPublicKey.size() == electoralBoardSchnorrProofs.size(),
				"The size of the electoral board public key must be equal to the size of the electoral board Schnorr proofs.");

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> publicKeys = Streams.concat(
						combinedControlComponentPublicKeys.stream()
								.map(ControlComponentPublicKeys::ccmjElectionPublicKey)
								.filter(ccmElectionPublicKey -> ccmElectionPublicKey.size() >= maxNumberOfWriteInFields + 1)
								.map(ccmElectionPublicKey ->
										new ElGamalMultiRecipientPublicKey(
												GroupVector.from(ccmElectionPublicKey.getKeyElements().subList(0, maxNumberOfWriteInFields + 1)))),
						Stream.of(electoralBoardPublicKey))
				.collect(GroupVector.toGroupVector());

		checkArgument(electionPublicKey.equals(elGamal.combinePublicKeys(publicKeys)),
				"Multiplication of the ccmElectionPublicKeys times the electoralBoardPublicKey must equal the electionPublicKey");

		final GqGroup encryptionGroup = combinedControlComponentPublicKeys.get(0).ccmjElectionPublicKey().getGroup();
		checkArgument(encryptionGroup.equals(electoralBoardPublicKey.getGroup()));
		checkArgument(encryptionGroup.equals(electionPublicKey.getGroup()));
		checkArgument(encryptionGroup.equals(choiceReturnCodesEncryptionPublicKey.getGroup()));
		checkArgument(encryptionGroup.hasSameOrderAs(electoralBoardSchnorrProofs.getGroup()));
	}

	@Override
	public List<? extends Hashable> toHashableForm() {
		return List.of(HashableList.from(combinedControlComponentPublicKeys), electoralBoardPublicKey, electoralBoardSchnorrProofs,
				electionPublicKey, choiceReturnCodesEncryptionPublicKey);
	}
}
