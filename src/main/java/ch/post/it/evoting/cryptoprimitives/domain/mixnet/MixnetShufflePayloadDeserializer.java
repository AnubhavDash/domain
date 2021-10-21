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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

/**
 * Deserializes a json into a {@link MixnetShufflePayload}. This deserializer is needed when deserializing a payload outside of a {@link
 * MixnetState}.
 */
class MixnetShufflePayloadDeserializer extends JsonDeserializer<MixnetShufflePayload> {

	@Override
	public MixnetShufflePayload deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final ObjectMapper mapper = (ObjectMapper) parser.getCodec();

		final JsonNode node = mapper.readTree(parser);
		final JsonNode encryptionGroupNode = node.get("encryptionGroup");
		final GqGroup gqGroup = mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);
		final String groupAttribute = "group";

		final VerifiableDecryptions verifiableDecryptions = mapper.reader()
				.withAttribute(groupAttribute, gqGroup)
				.readValue(node.get("verifiableDecryptions").toString(), VerifiableDecryptions.class);

		VerifiableShuffle verifiableShuffle = null;
		if (!node.path("verifiableShuffle").isMissingNode()) {
			verifiableShuffle = mapper.reader().withAttribute(groupAttribute, gqGroup)
					.readValue(node.get("verifiableShuffle").toString(), VerifiableShuffle.class);
		}

		final ElGamalMultiRecipientPublicKey remainingElectionPublicKey = mapper.reader().withAttribute(groupAttribute, gqGroup)
				.readValue(node.get("remainingElectionPublicKey").toString(), ElGamalMultiRecipientPublicKey.class);

		final ElGamalMultiRecipientPublicKey previousRemainingElectionPublicKey = mapper.reader().withAttribute(groupAttribute, gqGroup)
				.readValue(node.get("previousRemainingElectionPublicKey").toString(), ElGamalMultiRecipientPublicKey.class);

		final ElGamalMultiRecipientPublicKey nodeElectionPublicKey = mapper.reader().withAttribute(groupAttribute, gqGroup)
				.readValue(node.get("nodeElectionPublicKey").toString(), ElGamalMultiRecipientPublicKey.class);

		final int nodeId = mapper.readValue(node.get("nodeId").toString(), Integer.class);

		final CryptoPrimitivesPayloadSignature signature = mapper.reader().readValue(node.get("signature").toString(), CryptoPrimitivesPayloadSignature.class);

		return new MixnetShufflePayload(gqGroup, verifiableDecryptions, verifiableShuffle, remainingElectionPublicKey,
				previousRemainingElectionPublicKey, nodeElectionPublicKey, nodeId, signature);
	}
}
