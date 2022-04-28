/*
 * Copyright 2022 by Swiss Post, Information Technology
 */

package ch.post.it.evoting.cryptoprimitives.domain.signature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AliasTest {

	@ParameterizedTest
	@EnumSource(Alias.class)
	void testAliasGetByName(Alias alias) {
		// given
		final String aliasName = alias.get();

		// when
		final Alias aliasFromMap = Alias.getByComponentName(aliasName);

		// then
		assertEquals(alias, aliasFromMap);
	}

	@ParameterizedTest
	@ValueSource(strings = { "WRONG_NAME" })
	@NullSource
	void testAliasGetByNameWithInvalidName(String alias) {
		// given

		// when / then
		assertThrows(RuntimeException.class, () -> Alias.getByComponentName(alias));
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2, 3, 4 })
	void testAliasGetControlComponentByNodeIdWithValidValues(int nodeId) {
		// given
		Alias expectedAlias = Alias.getByComponentName("control_component_" + nodeId);

		// when
		final Alias alias = Alias.getControlComponentByNodeId(nodeId);

		// then
		assertEquals(expectedAlias, alias);
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 5 })
	void testAliasGetControlComponentByNodeIdWithInvalidValues(int nodeId) {
		// given

		// when / then
		assertThrows(IllegalArgumentException.class, () -> Alias.getControlComponentByNodeId(nodeId));
	}

	@ParameterizedTest
	@EnumSource(Alias.class)
	void validateAliasName(Alias alias) {
		// given
		Pattern validName = Pattern.compile("^([a-z0-9]+_)*[a-z0-9]+$");

		// when
		final String aliasName = alias.get();

		// then
		assertTrue(validName.matcher(aliasName).matches());
	}
}