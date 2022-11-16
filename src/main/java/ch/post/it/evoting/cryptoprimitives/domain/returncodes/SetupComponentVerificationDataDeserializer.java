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

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.EncryptionGroupUtils;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class SetupComponentVerificationDataDeserializer extends JsonDeserializer<SetupComponentVerificationDataPayload> {

	@Override
	public SetupComponentVerificationDataPayload deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final ObjectMapper mapper = (ObjectMapper) parser.getCodec();

		final JsonNode node = mapper.readTree(parser);
		final JsonNode encryptionGroupNode = node.get("encryptionGroup");
		final GqGroup encryptionGroup = EncryptionGroupUtils.getEncryptionGroup(mapper, encryptionGroupNode);
		final String groupAttribute = "group";

		final String electionEventId = mapper.readValue(node.get("electionEventId").toString(), String.class);
		final String verificationCardSetId = mapper.readValue(node.get("verificationCardSetId").toString(), String.class);
		final int chunkId = mapper.readValue(node.get("chunkId").toString(), Integer.class);

		final List<String> partialChoiceReturnCodesAllowList = Arrays.asList(
				mapper.readValue(node.get("partialChoiceReturnCodesAllowList").toString(), String[].class));

		final List<SetupComponentVerificationData> setupComponentVerificationData = Arrays.asList(mapper.reader()
				.withAttribute(groupAttribute, encryptionGroup)
				.readValue(node.get("setupComponentVerificationData").toString(), SetupComponentVerificationData[].class));

		final CombinedCorrectnessInformation combinedCorrectnessInformation = mapper.reader()
				.withAttribute(groupAttribute, encryptionGroup)
				.readValue(node.get("combinedCorrectnessInformation").toString(), CombinedCorrectnessInformation.class);

		final CryptoPrimitivesSignature signature = mapper.reader()
				.readValue(node.get("signature").toString(), CryptoPrimitivesSignature.class);

		return new SetupComponentVerificationDataPayload(electionEventId, verificationCardSetId, partialChoiceReturnCodesAllowList, chunkId,
				encryptionGroup, setupComponentVerificationData, combinedCorrectnessInformation, signature);
	}

}
