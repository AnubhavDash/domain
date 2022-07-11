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
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.election.ElectionEventContext;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class ElectionEventContextPayloadDeserializer extends JsonDeserializer<ElectionEventContextPayload> {

	@Override
	public ElectionEventContextPayload deserialize(final JsonParser parser, final DeserializationContext deserializationContext)
			throws IOException {

		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();

		final JsonNode node = mapper.readTree(parser);
		final JsonNode encryptionGroupNode = node.get("encryptionGroup");
		final GqGroup gqGroup = mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);

		final ElectionEventContext electionEventContext = mapper.reader().withAttribute("group", gqGroup)
				.readValue(node.get("electionEventContext"), ElectionEventContext.class);

		final CryptoPrimitivesSignature signature = mapper.readValue(node.get("signature").toString(), CryptoPrimitivesSignature.class);

		return new ElectionEventContextPayload(gqGroup, electionEventContext, signature);
	}
}
