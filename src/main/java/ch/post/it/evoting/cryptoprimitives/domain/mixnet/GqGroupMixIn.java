/*
 * Copyright 2021 Post CH Ltd
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

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "p", "q", "generator", "identity" })
public abstract class GqGroupMixIn {

	@JsonProperty
	@JsonSerialize(converter = BigIntegerToHexConverter.class)
	@JsonDeserialize(converter = HexToBigIntegerConverter.class)
	BigInteger p;

	@JsonProperty
	@JsonSerialize(converter = BigIntegerToHexConverter.class)
	@JsonDeserialize(converter = HexToBigIntegerConverter.class)
	BigInteger q;

	@JsonProperty("g")
	@JsonDeserialize(converter = HexToBigIntegerConverter.class)
	GqElement generator;

	@JsonIgnore
	GqElement identity;

	@JsonCreator
	GqGroupMixIn(
			@JsonProperty(value = "p", required = true)
			final BigInteger p,
			@JsonProperty(value = "q", required = true)
			final BigInteger q,
			@JsonProperty(value = "g", required = true)
			final BigInteger g) {
	}

}
