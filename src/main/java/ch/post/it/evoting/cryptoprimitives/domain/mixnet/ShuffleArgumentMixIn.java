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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ProductArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;

@SuppressWarnings({ "java:S100", "java:S116", "java:S117", "unused" })
@JsonPropertyOrder({ "c_A", "c_B", "productArgument", "multiExponentiationArgument" })
@JsonDeserialize(builder = ShuffleArgument.Builder.class)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class ShuffleArgumentMixIn {

	@JsonProperty
	GroupVector<GqElement, GqGroup> c_A;

	@JsonProperty
	GroupVector<GqElement, GqGroup> c_B;

	@JsonProperty
	ProductArgument productArgument;

	@JsonProperty
	MultiExponentiationArgument multiExponentiationArgument;

	@JsonPOJOBuilder(withPrefix = "with_")
	public interface ShuffleArgumentBuilderMixin {

		@JsonProperty
		@JsonDeserialize(using = GqGroupVectorDeserializer.class)
		ShuffleArgument.Builder with_c_A(final GroupVector<GqElement, GqGroup> c_A);

		@JsonProperty
		@JsonDeserialize(using = GqGroupVectorDeserializer.class)
		ShuffleArgument.Builder with_c_B(final GroupVector<GqElement, GqGroup> c_B);

	}

}
