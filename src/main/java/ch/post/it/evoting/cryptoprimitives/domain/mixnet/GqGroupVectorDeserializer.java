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
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

/**
 * Deserializes a json into a {@link GroupVector} of {@link GqElement}s.
 */
public class GqGroupVectorDeserializer extends JsonDeserializer<GroupVector<GqElement, GqGroup>> {

	/**
	 * The {@code context} must provide the {@link GqGroup} that will be used to reconstruct the various {@link GqElement}s.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public GroupVector<GqElement, GqGroup> deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		final GqGroup gqGroup = (GqGroup) context.getAttribute("group");
		final ObjectMapper mapper = new ObjectMapper();

		final GqElement[] elementsArray = mapper.addMixIn(GqElement.class, GqElementMixIn.class).reader().withAttribute("group", gqGroup)
				.readValue(parser, GqElement[].class);

		return GroupVector.from(Arrays.asList(elementsArray));
	}

}
