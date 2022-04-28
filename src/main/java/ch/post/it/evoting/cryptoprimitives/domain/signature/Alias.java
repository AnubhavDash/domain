/*
 * Copyright 2022 by Swiss Post, Information Technology
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
