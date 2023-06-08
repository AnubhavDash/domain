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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.domain.MapperSetUp;
import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;

@DisplayName("A ShuffleArgument")
class ShuffleArgumentMixInTest extends MapperSetUp {

	private static ShuffleArgument shuffleArgument;
	private static GqGroup gqGroup;

	@BeforeAll
	static void setUpAll() {
		gqGroup = SerializationTestData.getGqGroup();
		shuffleArgument = SerializationTestData.createShuffleArgument();
	}

	@Test
	@DisplayName("serialized then deserialized gives original ShuffleArgument")
	void cycle() throws IOException {
		final String serializedShuffleArgument = mapper.writeValueAsString(shuffleArgument);

		final ShuffleArgument deserializedShuffleArgument = mapper.reader().withAttribute("group", gqGroup)
				.readValue(serializedShuffleArgument, ShuffleArgument.class);

		assertEquals(shuffleArgument, deserializedShuffleArgument);
	}

	@Nested
	@DisplayName("with m=1")
	class WithMEqualOne {

		@Test
		@DisplayName("serialized then deserialized gives original ShuffleArgument")
		void cycle() throws IOException {
			final ShuffleArgument simpleShuffleArgument = SerializationTestData.createSimplestShuffleArgument();

			final String serializedShuffleArgument = mapper.writeValueAsString(simpleShuffleArgument);

			final ShuffleArgument deserializedShuffleArgument = mapper.reader().withAttribute("group", gqGroup)
					.readValue(serializedShuffleArgument, ShuffleArgument.class);

			assertEquals(simpleShuffleArgument, deserializedShuffleArgument);
		}

	}

}