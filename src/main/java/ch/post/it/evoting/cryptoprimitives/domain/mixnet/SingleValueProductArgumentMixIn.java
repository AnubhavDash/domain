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
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.SingleValueProductArgument;

@SuppressWarnings({ "java:S100", "java:S116", "java:S117", "java:S1845", "unused" })
@JsonPropertyOrder({ "c_d", "c_delta", "c_Delta", "a_tilde", "b_tilde", "r_tilde", "s_tilde" })
@JsonDeserialize(builder = SingleValueProductArgument.Builder.class)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class SingleValueProductArgumentMixIn {

	@JsonProperty
	GqElement c_d;

	@JsonProperty
	GqElement c_delta;

	@JsonProperty
	GqElement c_Delta;

	@JsonProperty
	GroupVector<ZqElement, ZqGroup> a_tilde;

	@JsonProperty
	GroupVector<ZqElement, ZqGroup> b_tilde;

	@JsonProperty
	ZqElement r_tilde;

	@JsonProperty
	ZqElement s_tilde;

	@JsonPOJOBuilder(withPrefix = "with_")
	public interface SingleValueProductArgumentBuilderMixIn {

		@JsonProperty
		@JsonDeserialize(using = ZqGroupVectorDeserializer.class)
		SingleValueProductArgument.Builder with_a_tilde(final GroupVector<ZqElement, ZqGroup> a_tilde);

		@JsonProperty
		@JsonDeserialize(using = ZqGroupVectorDeserializer.class)
		SingleValueProductArgument.Builder with_b_tilde(final GroupVector<ZqElement, ZqGroup> b_tilde);

	}

}
