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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class ElGamalMultiRecipientKeyPairDeserializer extends JsonDeserializer<ElGamalMultiRecipientKeyPair> {

	private static final String GROUP_ATTRIBUTE = "group";

	@Override
	public ElGamalMultiRecipientKeyPair deserialize(final JsonParser parser, final DeserializationContext context)
			throws IOException {

		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final GqGroup gqGroup = (GqGroup) context.getAttribute(GROUP_ATTRIBUTE);
		final JsonNode rootNode = mapper.readTree(parser);

		final ElGamalMultiRecipientPrivateKey privateKey = mapper.reader()
				.withAttribute(GROUP_ATTRIBUTE, gqGroup)
				.readValue(rootNode.get("privateKey"), ElGamalMultiRecipientPrivateKey.class);
		final ElGamalMultiRecipientPublicKey publicKey = mapper.reader()
				.withAttribute(GROUP_ATTRIBUTE, gqGroup)
				.readValue(rootNode.get("publicKey"), ElGamalMultiRecipientPublicKey.class);

		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.from(privateKey, publicKey.getGroup().getGenerator());

		if (!keyPair.getPublicKey().equals(publicKey)) {
			throw new IllegalArgumentException("The public key does not match the re-constructed key pair's public key.");
		}

		return keyPair;
	}
}
