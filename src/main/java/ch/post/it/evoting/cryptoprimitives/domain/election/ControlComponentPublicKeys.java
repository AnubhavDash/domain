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

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.SchnorrProofDeserializer;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.SchnorrProof;

public record ControlComponentPublicKeys(int nodeId,
										 ElGamalMultiRecipientPublicKey ccrjChoiceReturnCodesEncryptionPublicKey,
										 @JsonDeserialize(using = SchnorrProofDeserializer.class)
										 GroupVector<SchnorrProof, ZqGroup> ccrjSchnorrProofs,
										 ElGamalMultiRecipientPublicKey ccmjElectionPublicKey,
										 @JsonDeserialize(using = SchnorrProofDeserializer.class)
										 GroupVector<SchnorrProof, ZqGroup> ccmjSchnorrProofs) implements HashableList {

	public ControlComponentPublicKeys {
		checkArgument(NODE_IDS.contains(nodeId), String.format("The node id must be part of the known node ids. [nodeId :%s]", nodeId));
		checkNotNull(ccrjChoiceReturnCodesEncryptionPublicKey);
		checkNotNull(ccrjSchnorrProofs);
		checkArgument(ccrjChoiceReturnCodesEncryptionPublicKey.getGroup().hasSameOrderAs(ccrjSchnorrProofs.getGroup()),
				"The groups of the CCRj choice return codes encryption public key and the CCRj schnorr proofs must be of same order.");
		checkNotNull(ccmjElectionPublicKey);
		checkArgument(ccrjChoiceReturnCodesEncryptionPublicKey.getGroup().equals(ccmjElectionPublicKey.getGroup()),
				"The groups of the CCRj choice return codes encryption public key and the CCMj election public key must be equal.");
		checkNotNull(ccmjSchnorrProofs);
		checkArgument(ccmjElectionPublicKey.getGroup().hasSameOrderAs(ccmjSchnorrProofs.getGroup()),
				"The groups of the CCMj election public key and the CCMj schnorr proofs must be of same order.");
	}

	@Override
	public List<Hashable> toHashableForm() {
		return List.of(
				HashableBigInteger.from(BigInteger.valueOf(nodeId)),
				ccrjChoiceReturnCodesEncryptionPublicKey,
				ccrjSchnorrProofs,
				ccmjElectionPublicKey,
				ccmjSchnorrProofs);
	}
}