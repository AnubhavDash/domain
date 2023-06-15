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
package ch.post.it.evoting.cryptoprimitives.domain.signature;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Supplier;

/**
 * Alias of the participants of direct trust.
 */
public enum Alias implements Supplier<String> {
	CANTON("canton"),
	SDM_CONFIG("sdm_config"),
	SDM_TALLY("sdm_tally"),
	VOTING_SERVER("voting_server"),
	CONTROL_COMPONENT_1("control_component_1"),
	CONTROL_COMPONENT_2("control_component_2"),
	CONTROL_COMPONENT_3("control_component_3"),
	CONTROL_COMPONENT_4("control_component_4");

	private static final Map<String, Alias> ALIAS_MAP = Stream.of(Alias.values())
			.collect(Collectors.toMap(alias -> alias.componentName, alias -> alias));

	private final String componentName;

	public static Alias getByComponentName(final String componentName) {
		checkNotNull(componentName);
		checkArgument(ALIAS_MAP.containsKey(componentName), String.format("Alias '%s' does not exist.", componentName));
		return ALIAS_MAP.get(componentName);
	}

	public static Alias getControlComponentByNodeId(int nodeId) {
		checkArgument(nodeId > 0 && nodeId <= 4, "The node ID must be in the range (0,4].");
		String aliasName = "control_component_" + nodeId;
		return ALIAS_MAP.get(aliasName);
	}

	Alias(final String componentName) {
		this.componentName = componentName;
	}

	@Override
	public String get() {
		return componentName;
	}
}
