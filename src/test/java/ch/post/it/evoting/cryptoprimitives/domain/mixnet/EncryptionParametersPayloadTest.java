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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;
import ch.post.it.evoting.cryptoprimitives.math.Random;
import ch.post.it.evoting.cryptoprimitives.math.RandomFactory;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

class EncryptionParametersPayloadTest extends TestGroupSetup {

	private static final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	private static final Random random = RandomFactory.createRandom();

	private static EncryptionParametersPayload encryptionParametersPayload;
	private static ObjectNode rootNode;

	private final GqGroup encryptionGroup = GroupTestData.getGroupP59();

	private String seed;
	private GroupVector<PrimeGqElement, GqGroup> smallPrimes;
	private CryptoPrimitivesSignature signature;

	@BeforeEach
	void setup() throws JsonProcessingException {
		final int desiredNumberOfPrimes = 7;

		// Create payload.
		seed = random.genRandomInteger(BigInteger.valueOf(267)).toString();
		smallPrimes = PrimeGqElement.PrimeGqElementFactory.getSmallPrimeGroupMembers(encryptionGroup, desiredNumberOfPrimes);
		signature = new CryptoPrimitivesSignature("signature".getBytes(StandardCharsets.UTF_8));

		encryptionParametersPayload = new EncryptionParametersPayload(encryptionGroup, seed, smallPrimes, signature);

		// Create expected Json.
		rootNode = mapper.createObjectNode();
		rootNode.set("encryptionGroup", mapper.readTree(mapper.writeValueAsString(encryptionGroup)));
		rootNode.put("seed", seed);
		rootNode.set("smallPrimes", mapper.readTree(mapper.writeValueAsString(smallPrimes)));
		rootNode.set("signature", mapper.readTree(mapper.writeValueAsString(signature)));

	}

	@Test
	@DisplayName("check that smallPrimes are in ascending order")
	void checkSmallprimesOrder() {

		// Create payload with prime not in ascending order.
		final GroupVector<PrimeGqElement, GqGroup> smallPrimesNonAscending = GroupVector.of(
				smallPrimes.get(1),
				smallPrimes.get(0),
				smallPrimes.get(2),
				smallPrimes.get(3),
				smallPrimes.get(4));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new EncryptionParametersPayload(encryptionGroup, seed, smallPrimesNonAscending, signature));

		assertEquals("The elements of smallPrimes must be in ascending order.", ex.getMessage());

	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(encryptionParametersPayload);
		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws IOException {
		final String serialized = rootNode.toString();
		final EncryptionParametersPayload deserializedPayload = mapper.readValue(serialized, EncryptionParametersPayload.class);
		assertEquals(encryptionParametersPayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final String serialized = mapper.writeValueAsString(encryptionParametersPayload);
		final EncryptionParametersPayload deserializedPayload = mapper.readValue(serialized, EncryptionParametersPayload.class);

		assertEquals(encryptionParametersPayload, deserializedPayload);
	}
}