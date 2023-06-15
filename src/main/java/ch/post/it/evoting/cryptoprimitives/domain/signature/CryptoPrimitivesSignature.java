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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

public record CryptoPrimitivesSignature(byte[] signatureContents) {

	/**
	 * Creates the representation of a crypto-primitives signature.
	 *
	 * @param signatureContents the byte array containing the signature
	 */
	public CryptoPrimitivesSignature(final byte[] signatureContents) {
		checkNotNull(signatureContents);

		this.signatureContents = Arrays.copyOf(signatureContents, signatureContents.length);
	}

	@Override
	public byte[] signatureContents() {
		return Arrays.copyOf(signatureContents, signatureContents.length);
	}

	@Override
	public String toString() {
		return "CryptoPrimitivesSignature{" +
				"signatureContents=" + Arrays.toString(signatureContents) +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CryptoPrimitivesSignature that = (CryptoPrimitivesSignature) o;
		return Arrays.equals(signatureContents, that.signatureContents);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(signatureContents);
	}

}
