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
package ch.post.it.evoting.cryptoprimitives.domain.election;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {
	private final String id;

	private final Integer max;

	private final Integer min;

	private final Integer cumul;

	private final boolean writeIn;

	private final List<List<String>> fusions;

	private final String blankAttribute;

	private final String attribute;

	private final String writeInAttribute;

	@JsonCreator
	public Question(
			@JsonProperty("id")
					String id,
			@JsonProperty("max")
					Integer max,
			@JsonProperty("min")
					Integer min,
			@JsonProperty("cumul")
					Integer cumul,
			@JsonProperty("writeIn")
					String writeIn,
			@JsonProperty("fusions")
					List<List<String>> fusions,
			@JsonProperty("blankAttribute")
					String blankAttribute,
			@JsonProperty("writeInAttribute")
					String writeInAttribute,
			@JsonProperty("attribute")
					String attribute) {
		super();
		this.id = id;
		this.max = max;
		this.min = min;
		this.cumul = cumul;
		this.writeIn = Boolean.valueOf(writeIn);
		this.fusions = fusions;
		this.blankAttribute = blankAttribute;
		this.attribute = attribute;
		this.writeInAttribute = writeInAttribute;
	}

	@JsonGetter("id")
	public String getId() {
		return id;
	}

	@JsonGetter("max")
	public Integer getMax() {
		return max;
	}

	@JsonGetter("min")
	public Integer getMin() {
		return min;
	}

	@JsonGetter("cumul")
	public Integer getCumul() {
		return cumul;
	}

	@JsonGetter("writeIn")
	public boolean isWriteIn() {
		return writeIn;
	}

	@JsonGetter("fusions")
	public List<List<String>> getFusions() {
		return fusions;
	}

	@JsonGetter("blankAttribute")
	public String getBlankAttribute() {
		return blankAttribute;
	}

	@JsonGetter("attribute")
	public String getAttribute() {
		return attribute;
	}

	@JsonGetter("writeInAttribute")
	public String getWriteInAttribute() {
		return writeInAttribute;
	}

}
