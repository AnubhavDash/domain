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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class ControlComponentCodeSharesPayloadDeserializer extends JsonDeserializer<ControlComponentCodeSharesPayload> {

	@Override
	public ControlComponentCodeSharesPayload deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final ObjectMapper mapper = (ObjectMapper) parser.getCodec();

		final JsonNode node = mapper.readTree(parser);
		final JsonNode encryptionGroupNode = node.get("encryptionGroup");
		final GqGroup gqGroup = mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);

		final String electionEventId = mapper.readValue(node.get("electionEventId").toString(), String.class);
		final String verificationCardSetId = mapper.readValue(node.get("verificationCardSetId").toString(), String.class);
		final int chunkId = mapper.readValue(node.get("chunkId").toString(), Integer.class);

		final List<ControlComponentCodeShare> returnCodeGenerationInputs = Arrays.asList(mapper.reader().withAttribute("group", gqGroup)
				.readValue(node.get("controlComponentCodeShares").toString(), ControlComponentCodeShare[].class));

		final int nodeId = mapper.readValue(node.get("nodeId").toString(), Integer.class);

		final CryptoPrimitivesSignature signature = mapper.reader().readValue(node.get("signature").toString(), CryptoPrimitivesSignature.class);

		return new ControlComponentCodeSharesPayload(electionEventId, verificationCardSetId, chunkId, gqGroup, returnCodeGenerationInputs,
				nodeId, signature);
	}

}
