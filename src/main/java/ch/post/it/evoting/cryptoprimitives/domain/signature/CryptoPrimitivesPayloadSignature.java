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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CryptoPrimitivesPayloadSignature {

	@JsonProperty
	private final byte[] signatureContents;

	@JsonSerialize(contentUsing = PemSerializer.class)
	@JsonDeserialize(contentUsing = PemDeserializer.class)
	private final X509Certificate[] certificateChain;

	/**
	 * Creates the representation of a crypto-primitives signature.
	 *
	 * @param signatureContents the byte stream containing the signature
	 * @param certificateChain  the certificate chain to be used when validating the signature
	 */
	@JsonCreator
	public CryptoPrimitivesPayloadSignature(
			@JsonProperty(value = "signatureContents", required = true)
					byte[] signatureContents,
			@JsonProperty(value = "certificateChain", required = true)
					X509Certificate[] certificateChain) {
		this.signatureContents = checkNotNull(signatureContents);
		this.certificateChain = checkNotNull(certificateChain);
	}

	/**
	 * Gets a certificate chain whose last element is the public key used to validate the signature.
	 *
	 * @return the certificate chain
	 */
	public X509Certificate[] getCertificateChain() {
		return certificateChain;
	}

	/**
	 * @return the byte array representing the signature
	 */
	public byte[] getSignatureContents() {
		return signatureContents;
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
				throw new UncheckedIOException("Can not convert PEM string " + pemString + " to object of type {@link " + X509CertificateHolder.class + "}", e);
			}

			try {
				return new JcaX509CertificateConverter().getCertificate(certificateHolder);
			} catch (CertificateException e) {
				throw new IOException("Can not extract the X509Certificate from the certificate holder", e);
			}
		}
	}

}
