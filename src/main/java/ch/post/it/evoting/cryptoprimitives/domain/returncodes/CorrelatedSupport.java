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
package ch.post.it.evoting.cryptoprimitives.domain.returncodes;

import java.util.Objects;
import java.util.UUID;

public class CorrelatedSupport  {
	private UUID correlationId;

	/**
	 * Constructor.
	 */
	public CorrelatedSupport() {
	}

	/**
	 * Constructor.
	 *
	 * @param correlationId the correlation identifier.
	 */
	public CorrelatedSupport(UUID correlationId) {
		this.correlationId = correlationId;
	}

	public UUID getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(UUID correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CorrelatedSupport that = (CorrelatedSupport) o;
		return Objects.equals(correlationId, that.correlationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(correlationId);
	}
}
