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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ControlComponentCodeSharesPayloadSerializer extends JsonSerializer<ControlComponentCodeSharesPayload> {

	@Override
	public void serialize(final ControlComponentCodeSharesPayload value, final JsonGenerator gen, final SerializerProvider serializers)
			throws IOException {
		final ObjectMapper mapper = (ObjectMapper) gen.getCodec();

		gen.writeStartObject();

		gen.writeStringField("electionEventId", value.getElectionEventId());
		gen.writeStringField("verificationCardSetId", value.getVerificationCardSetId());
		gen.writeNumberField("chunkId", value.getChunkId());
		gen.writeFieldName("encryptionGroup");
		final String encryptionGroup = mapper.writeValueAsString(value.getEncryptionGroup());
		gen.writeRawValue(encryptionGroup);

		gen.writeFieldName("controlComponentCodeShares");
		final String controlComponentCodeShares = mapper
				.writer().withAttribute("base64Conversion", true)
				.writeValueAsString(value.getControlComponentCodeShares());
		gen.writeRawValue(controlComponentCodeShares);

		gen.writeNumberField("nodeId", value.getNodeId());
		gen.writeFieldName("signature");
		final String signature = mapper.writeValueAsString(value.getSignature());
		gen.writeRawValue(signature);

		gen.writeEndObject();
	}
}
