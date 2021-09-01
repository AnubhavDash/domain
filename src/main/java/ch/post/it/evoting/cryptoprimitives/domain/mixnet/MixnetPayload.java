/*
 * (c) Copyright 2021 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;

/**
 * Represents a mixnet payload. This payload is the input or the output of a mixing / decryption operation.
 */
@JsonDeserialize(using = MixnetPayloadDeserializer.class)
public interface MixnetPayload {

	List<ElGamalMultiRecipientCiphertext> getEncryptedVotes();

	ElGamalMultiRecipientPublicKey getRemainingElectionPublicKey();

	CryptoPrimitivesPayloadSignature getSignature();

	void setSignature(final CryptoPrimitivesPayloadSignature signature);
}
