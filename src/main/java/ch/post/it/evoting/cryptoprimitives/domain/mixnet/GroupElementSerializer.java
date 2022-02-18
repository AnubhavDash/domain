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

import static ch.post.it.evoting.cryptoprimitives.domain.mixnet.ConversionUtils.bigIntegerToHex;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ch.post.it.evoting.cryptoprimitives.math.GroupElement;

/**
 * Serializes a {@link GroupElement} to a standalone string, i.e. by itself it does not return a valid JSON. We consider the GroupElement as a
 * primitive and not an object, hence it cannot be directly serialized to a JSON with this method.
 */
class GroupElementSerializer extends JsonSerializer<GroupElement<?>> {

	@Override
	public void serialize(final GroupElement element, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
		gen.writeString(bigIntegerToHex(element.getValue()));
	}

}
