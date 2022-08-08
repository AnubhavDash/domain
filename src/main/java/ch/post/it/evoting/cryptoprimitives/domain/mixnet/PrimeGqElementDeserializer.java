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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;

/**
 * Deserializes a json to a {@link PrimeGqElement}.
 */
class PrimeGqElementDeserializer extends JsonDeserializer<PrimeGqElement> {

	/**
	 * The {@code context} must provide the {@link GqGroup} that will be used to reconstruct the various {@link PrimeGqElement}s.
	 *
	 * @inheritDoc
	 */
	@Override
	public PrimeGqElement deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final GqGroup gqGroup = (GqGroup) context.getAttribute("group");
		final JsonNode node = new ObjectMapper().readTree(parser);
		checkArgument(node.isInt(), "The serialized primeGqElement is not an int");
		final int value = node.asInt();

		return PrimeGqElement.PrimeGqElementFactory.fromValue(value, gqGroup);
	}

}
