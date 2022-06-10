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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record CryptoPrimitivesPayloadSignature(
		byte[] signatureContents,
		@JsonSerialize(contentUsing = PemSerializer.class)
		@JsonDeserialize(contentUsing = PemDeserializer.class)
		X509Certificate[] certificateChain) {

	/**
	 * Creates the representation of a crypto-primitives signature.
	 *
	 * @param signatureContents the byte stream containing the signature
	 * @param certificateChain  the certificate chain to be used when validating the signature. Null if using direct trust.
	 */
	public CryptoPrimitivesPayloadSignature {
		checkNotNull(signatureContents);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CryptoPrimitivesPayloadSignature that = (CryptoPrimitivesPayloadSignature) o;
		return Arrays.equals(signatureContents, that.signatureContents) && Arrays.equals(certificateChain, that.certificateChain);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(signatureContents);
		result = 31 * result + Arrays.hashCode(certificateChain);
		return result;
	}

	@Override
	public String toString() {
		return "CryptoPrimitivesPayloadSignature{" +
				"signatureContents=" + Arrays.toString(signatureContents) +
				", certificateChain=" + Arrays.toString(certificateChain) +
				'}';
	}

	private static class PemSerializer extends JsonSerializer<X509Certificate> {
		@Override
		public void serialize(X509Certificate value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
			checkNotNull(value);
			StringWriter pemStringWriter = new StringWriter();
			try (JcaPEMWriter writer = new JcaPEMWriter(pemStringWriter)) {
				writer.writeObject(value);
			} catch (IOException e) {
				throw new UncheckedIOException("Could not convert object of type {@link " + X509Certificate.class + "} to PEM format.", e);
			}
			generator.writeString(pemStringWriter.toString());
		}
	}

	private static class PemDeserializer extends JsonDeserializer<X509Certificate> {
		@Override
		public X509Certificate deserialize(JsonParser p, DeserializationContext context) throws IOException {
			String pemString = p.readValueAs(String.class);
			checkNotNull(pemString);

			X509CertificateHolder certificateHolder;
			try (PEMParser parser = new PEMParser(new StringReader(pemString))) {
				certificateHolder = (X509CertificateHolder) parser.readObject();
			} catch (IOException e) {
				throw new UncheckedIOException(
						"Can not convert PEM string " + pemString + " to object of type {@link " + X509CertificateHolder.class + "}", e);
			}

			try {
				return new JcaX509CertificateConverter().getCertificate(certificateHolder);
			} catch (CertificateException e) {
				throw new IOException("Can not extract the X509Certificate from the certificate holder", e);
			}
		}
	}

}
