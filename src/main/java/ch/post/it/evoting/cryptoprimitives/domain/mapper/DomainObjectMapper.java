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
package ch.post.it.evoting.cryptoprimitives.domain.mapper;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.DecryptionProofMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ElGamalMultiRecipientCiphertextMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ElGamalMultiRecipientKeyPairMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ElGamalMultiRecipientMessageMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ElGamalMultiRecipientPrivateKeyMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ElGamalMultiRecipientPublicKeyMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ExponentiationProofMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.GqElementMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.GqGroupMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.HadamardArgumentMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MultiExponentiationArgumentMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.PlaintextEqualityProofMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ProductArgumentMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ShuffleArgumentMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.SingleValueProductArgumentMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.VerifiableDecryptionsMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.VerifiableShuffleMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ZeroArgumentMixIn;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ZqElementMixIn;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.mixnet.HadamardArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ProductArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.SingleValueProductArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.mixnet.ZeroArgument;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

/**
 * Global configuration of the {@link ObjectMapper} needed to serialize/deserialize the mixnet domain objects. This configuration adds all necessary
 * mixIns and serializers.
 */
public class DomainObjectMapper {

	private DomainObjectMapper() {
		// Intentionally left blank.
	}

	/**
	 * @return a new instance of an already configured {@link ObjectMapper}.
	 */
	public static ObjectMapper getNewInstance() {
		return new ObjectMapper()
				// Primitive elements.
				.addMixIn(GqElement.class, GqElementMixIn.class).addMixIn(ZqElement.class, ZqElementMixIn.class)
				.addMixIn(GqGroup.class, GqGroupMixIn.class)
				// ElGamal objects.
				.addMixIn(ElGamalMultiRecipientCiphertext.class, ElGamalMultiRecipientCiphertextMixIn.class)
				.addMixIn(ElGamalMultiRecipientMessage.class, ElGamalMultiRecipientMessageMixIn.class)
				.addMixIn(ElGamalMultiRecipientPublicKey.class, ElGamalMultiRecipientPublicKeyMixIn.class)
				.addMixIn(ElGamalMultiRecipientPrivateKey.class, ElGamalMultiRecipientPrivateKeyMixIn.class)
				.addMixIn(ElGamalMultiRecipientKeyPair.class, ElGamalMultiRecipientKeyPairMixIn.class)
				// Verifiable.
				.addMixIn(VerifiableShuffle.class, VerifiableShuffleMixIn.class)
				.addMixIn(VerifiableDecryptions.class, VerifiableDecryptionsMixIn.class)
				// Proofs.
				.addMixIn(DecryptionProof.class, DecryptionProofMixIn.class)
				.addMixIn(ExponentiationProof.class, ExponentiationProofMixIn.class)
				.addMixIn(PlaintextEqualityProof.class, PlaintextEqualityProofMixIn.class)
				// Arguments.
				.addMixIn(ShuffleArgument.class, ShuffleArgumentMixIn.class)
				.addMixIn(MultiExponentiationArgument.class, MultiExponentiationArgumentMixIn.class)
				.addMixIn(ProductArgument.class, ProductArgumentMixIn.class)
				.addMixIn(SingleValueProductArgument.class, SingleValueProductArgumentMixIn.class)
				.addMixIn(HadamardArgument.class, HadamardArgumentMixIn.class).addMixIn(ZeroArgument.class, ZeroArgumentMixIn.class)
				// Arguments Builders.
				.addMixIn(ShuffleArgument.Builder.class, ShuffleArgumentMixIn.ShuffleArgumentBuilderMixin.class)
				.addMixIn(MultiExponentiationArgument.Builder.class, MultiExponentiationArgumentMixIn.MultiExponentiationArgumentBuilderMixIn.class)
				.addMixIn(SingleValueProductArgument.Builder.class, SingleValueProductArgumentMixIn.SingleValueProductArgumentBuilderMixIn.class)
				.addMixIn(ZeroArgument.Builder.class, ZeroArgumentMixIn.ZeroArgumentBuilderMixIn.class)
				.disable(MapperFeature.USE_GETTERS_AS_SETTERS).registerModule(new JavaTimeModule());
	}

}
