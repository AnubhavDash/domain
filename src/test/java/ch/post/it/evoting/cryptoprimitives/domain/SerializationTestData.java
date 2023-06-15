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
package ch.post.it.evoting.cryptoprimitives.domain;

import static ch.post.it.evoting.cryptoprimitives.domain.ConversionUtils.bigIntegerToBase64;
import static ch.post.it.evoting.cryptoprimitives.domain.ConversionUtils.bigIntegerToHex;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.VerifiablePlaintextDecryption;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ControlComponentCodeShare;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ControlComponentCodeSharesPayload;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SetupComponentVerificationData;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SetupComponentVerificationDataPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.BaseEncodingFactory;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.HadamardArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ProductArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.SingleValueProductArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.mixnet.ZeroArgument;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.SchnorrProof;

public class SerializationTestData {

	private static final ObjectMapper mapper = DomainObjectMapper.getNewInstance();

	private static final GqGroup gqGroup = getGqGroup();
	private static final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
	private static final BigInteger ZERO = BigInteger.valueOf(0);
	private static final BigInteger ONE = BigInteger.valueOf(1);
	private static final BigInteger TWO = BigInteger.valueOf(2);
	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FOUR = BigInteger.valueOf(4);
	private static final BigInteger FIVE = BigInteger.valueOf(5);
	private static final BigInteger NINE = BigInteger.valueOf(9);
	private static final ZqElement zZero = ZqElement.create(ZERO, zqGroup);
	private static final ZqElement zOne = ZqElement.create(ONE, zqGroup);
	private static final ZqElement zTwo = ZqElement.create(TWO, zqGroup);
	private static final ZqElement zThree = ZqElement.create(THREE, zqGroup);
	private static final ZqElement zFour = ZqElement.create(FOUR, zqGroup);
	private static final GqElement gOne = GqElementFactory.fromValue(ONE, gqGroup);
	private static final GqElement gThree = GqElementFactory.fromValue(THREE, gqGroup);
	private static final GqElement gFour = GqElementFactory.fromValue(FOUR, gqGroup);
	private static final GqElement gFive = GqElementFactory.fromValue(FIVE, gqGroup);
	private static final GqElement gNine = GqElementFactory.fromValue(NINE, gqGroup);

	private SerializationTestData() {
		// Intentionally left blank.
	}

	/**
	 * This group is only compatible with securityLevel TESTING_ONLY
	 */
	public static GqGroup getGqGroup() {
		return new GqGroup(BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(3));
	}

	// ===============================================================================================================================================
	// Basic objects to serialize creation.
	// ===============================================================================================================================================

	public static ElGamalMultiRecipientMessage getMessage() {
		final GroupVector<GqElement, GqGroup> messageElements = GroupVector.of(
				GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup),
				GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup));

		return new ElGamalMultiRecipientMessage(messageElements);
	}

	public static GroupVector<ElGamalMultiRecipientMessage, GqGroup> getMessages(final int nbr) {
		return Collections.nCopies(nbr, getMessage()).stream().collect(GroupVector.toGroupVector());
	}

	public static List<ElGamalMultiRecipientCiphertext> getCiphertexts(final int nbr) {
		final GqElement gamma = GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup);
		final List<GqElement> phis = Arrays
				.asList(GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup), GqElementFactory.fromValue(BigInteger.valueOf(9), gqGroup));
		final ElGamalMultiRecipientCiphertext ciphertext = ElGamalMultiRecipientCiphertext.create(gamma, phis);

		return Collections.nCopies(nbr, ciphertext);
	}

	public static ElGamalMultiRecipientCiphertext getSinglePhiCiphertext() {
		final GqElement gamma = GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup);
		final List<GqElement> phis = Collections.singletonList(GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup));

		return ElGamalMultiRecipientCiphertext.create(gamma, phis);
	}

	public static GroupVector<DecryptionProof, ZqGroup> getDecryptionProofs(final int nbr) {
		final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
		final ZqElement e = ZqElement.create(2, zqGroup);
		final GroupVector<ZqElement, ZqGroup> z = GroupVector.of(ZqElement.create(1, zqGroup), ZqElement.create(3, zqGroup));
		final DecryptionProof decryptionProof = new DecryptionProof(e, z);

		return Collections.nCopies(nbr, decryptionProof).stream().collect(GroupVector.toGroupVector());
	}

	public static ElGamalMultiRecipientPublicKey getPublicKey() {
		final GroupVector<GqElement, GqGroup> keyElements = GroupVector.of(
				GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup),
				GqElementFactory.fromValue(BigInteger.valueOf(9), gqGroup));
		return new ElGamalMultiRecipientPublicKey(keyElements);
	}

	public static ElGamalMultiRecipientPublicKey getSingleElementPublicKey() {
		final GroupVector<GqElement, GqGroup> keyElements = GroupVector.of(GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup));
		return new ElGamalMultiRecipientPublicKey(keyElements);
	}

	public static ElGamalMultiRecipientPrivateKey getPrivateKey() {
		final GroupVector<ZqElement, ZqGroup> keyElements = GroupVector.of(
				ZqElement.create(BigInteger.valueOf(2), zqGroup),
				ZqElement.create(BigInteger.valueOf(3), zqGroup));
		return new ElGamalMultiRecipientPrivateKey(keyElements);
	}

	public static VerifiableShuffle getVerifiableShuffle(final int ciphertextsNbr) {
		final List<ElGamalMultiRecipientCiphertext> ciphertexts = getCiphertexts(ciphertextsNbr);
		final ShuffleArgument shuffleArgument = createShuffleArgument();

		return new VerifiableShuffle(GroupVector.from(ciphertexts), shuffleArgument);
	}

	public static VerifiablePlaintextDecryption getVerifiablePlaintextDecryption(final int messagesNbr) {
		final GroupVector<ElGamalMultiRecipientMessage, GqGroup> messages = getMessages(messagesNbr);
		final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = getDecryptionProofs(messagesNbr);

		return new VerifiablePlaintextDecryption(messages, decryptionProofs);
	}

	public static ExponentiationProof createExponentiationProof() {
		final ZqElement e = ZqElement.create(1, zqGroup);
		final ZqElement z = ZqElement.create(3, zqGroup);

		return new ExponentiationProof(e, z);
	}

	public static GroupVector<SchnorrProof, ZqGroup> createSchnorrProofs(final int copyNumber) {
		final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
		final ZqElement e = ZqElement.create(2, zqGroup);
		final ZqElement z = ZqElement.create(2, zqGroup);
		final SchnorrProof schnorrProof = new SchnorrProof(e, z);

		return Collections.nCopies(copyNumber, schnorrProof).stream().collect(GroupVector.toGroupVector());
	}

	// ===============================================================================================================================================
	// Nodes creation.
	// ===============================================================================================================================================

	public static JsonNode createEncryptionGroupNode(final GqGroup gqGroup) {
		final JsonNode encryptionGroupNode;
		try {
			encryptionGroupNode = mapper.readTree(mapper.writeValueAsString(gqGroup));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to create encryptionGroup node.");
		}

		return encryptionGroupNode;
	}

	public static ObjectNode createMessageNode(final ElGamalMultiRecipientMessage elGamalMultiRecipientMessage) {
		final ObjectNode rootNode = mapper.createObjectNode();

		final ArrayNode messageArrayNode = rootNode.putArray("message");
		elGamalMultiRecipientMessage.stream().forEach(element -> messageArrayNode.add(bigIntegerToHex(element.getValue())));

		return rootNode;
	}

	public static ArrayNode createMessagesNode(final List<ElGamalMultiRecipientMessage> messages) {
		final ArrayNode messageArrayNode = mapper.createArrayNode();
		messages.forEach(m -> messageArrayNode.add(createMessageNode(m)));

		return messageArrayNode;
	}

	public static ObjectNode createCiphertextNode(final ElGamalMultiRecipientCiphertext ciphertext) {
		final GqElement gamma = ciphertext.getGamma();
		final List<GqElement> phis = ciphertext.getPhis();
		final ObjectNode ciphertextNode = mapper.createObjectNode().put("gamma", bigIntegerToHex(gamma.getValue()));
		final ArrayNode phisArrayNode = ciphertextNode.putArray("phis");
		for (GqElement phi : phis) {
			phisArrayNode.add(bigIntegerToHex(phi.getValue()));
		}

		return ciphertextNode;
	}

	public static ObjectNode createCiphertextNodeBase64(final ElGamalMultiRecipientCiphertext ciphertext) {
		final GqElement gamma = ciphertext.getGamma();
		final List<GqElement> phis = ciphertext.getPhis();

		final ObjectNode ciphertextNode = mapper.createObjectNode().put("gamma", bigIntegerToBase64(gamma.getValue()));
		final ArrayNode phisArrayNode = ciphertextNode.putArray("phis");
		for (GqElement phi : phis) {
			phisArrayNode.add(bigIntegerToBase64(phi.getValue()));
		}

		return ciphertextNode;
	}

	public static ArrayNode createCiphertextsNode(final List<ElGamalMultiRecipientCiphertext> ciphertexts) {
		final ArrayNode ciphertextsArrayNode = mapper.createArrayNode();
		for (ElGamalMultiRecipientCiphertext ciphertext : ciphertexts) {
			ciphertextsArrayNode.add(createCiphertextNode(ciphertext));
		}

		return ciphertextsArrayNode;
	}

	public static ArrayNode createDecryptionProofsNode(final GroupVector<DecryptionProof, ZqGroup> decryptionProofs) {
		final ArrayNode decryptionProofsArrayNode = mapper.createArrayNode();

		final List<JsonNode> proofsNodes = decryptionProofs.stream().map(proof -> {
			try {
				return mapper.readTree(mapper.writeValueAsString(proof));
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Failed to serialize proofs.");
			}
		}).toList();

		for (JsonNode jsonNode : proofsNodes) {
			decryptionProofsArrayNode.add(jsonNode);
		}

		return decryptionProofsArrayNode;
	}

	public static ArrayNode createExponentiationProofsNode(final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs) {
		final ArrayNode exponentiationProofsArrayNode = mapper.createArrayNode();

		final List<JsonNode> proofsNodes = exponentiationProofs.stream().map(proof -> {
			try {
				return mapper.readTree(mapper.writeValueAsString(proof));
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Failed to serialize proofs.");
			}
		}).toList();

		for (JsonNode jsonNode : proofsNodes) {
			exponentiationProofsArrayNode.add(jsonNode);
		}

		return exponentiationProofsArrayNode;
	}

	public static ArrayNode createPublicKeyNode(final ElGamalMultiRecipientPublicKey publicKey) {
		final ArrayNode keyArrayNode = mapper.createArrayNode();
		for (int i = 0; i < publicKey.size(); i++) {
			keyArrayNode.add(bigIntegerToHex(publicKey.get(i).getValue()));
		}

		return keyArrayNode;
	}

	public static ArrayNode createPublicKeyNodeBase64(final ElGamalMultiRecipientPublicKey publicKey) {
		final ArrayNode keyArrayNode = mapper.createArrayNode();
		for (int i = 0; i < publicKey.size(); i++) {
			keyArrayNode.add(bigIntegerToBase64(publicKey.get(i).getValue()));
		}

		return keyArrayNode;
	}

	public static ArrayNode createPrivateKeyNode(final ElGamalMultiRecipientPrivateKey privateKey) {
		final ArrayNode keyArrayNode = mapper.createArrayNode();
		for (int i = 0; i < privateKey.size(); i++) {
			keyArrayNode.add(bigIntegerToHex(privateKey.get(i).getValue()));
		}

		return keyArrayNode;
	}

	public static ObjectNode createVerifiableShuffleNode(final VerifiableShuffle verifiableShuffle) {
		final ObjectNode rootNode = mapper.createObjectNode();

		final ArrayNode shuffledCiphertextsNode = SerializationTestData.createCiphertextsNode(verifiableShuffle.shuffledCiphertexts());
		rootNode.set("shuffledCiphertexts", shuffledCiphertextsNode);

		final JsonNode shuffleArgumentNode = createShuffleArgumentNode();
		rootNode.set("shuffleArgument", shuffleArgumentNode);

		return rootNode;
	}

	public static ObjectNode createVerifiablePlaintextDecryptionNode(final VerifiablePlaintextDecryption verifiablePlaintextDecryption) {
		final ObjectNode rootNode = mapper.createObjectNode();

		final ArrayNode messagesNode = createMessagesNode(verifiablePlaintextDecryption.getDecryptedVotes());
		rootNode.set("decryptedVotes", messagesNode);

		final ArrayNode decryptionProofsNode = createDecryptionProofsNode(verifiablePlaintextDecryption.getDecryptionProofs());
		rootNode.set("decryptionProofs", decryptionProofsNode);

		return rootNode;
	}

	// ===============================================================================================================================================
	// Arguments creation.
	// ===============================================================================================================================================

	public static ShuffleArgument createShuffleArgument() {
		// This is an example for m,n,l = 2.

		// ZeroArgument.
		final ZeroArgument zeroArgument = new ZeroArgument.Builder().with_c_A_0(gNine).with_c_B_m(gFive)
				.with_c_d(GroupVector.of(gFive, gNine, gFour, gOne, gFour)).with_a_prime(GroupVector.of(zThree, zTwo))
				.with_b_prime(GroupVector.of(zFour, zThree)).with_r_prime(zOne).with_s_prime(zThree).with_t_prime(zOne).build();

		// HadamardArgument.
		GroupVector<GqElement, GqGroup> commitmentsB = GroupVector.of(gNine, gFive);
		final HadamardArgument hadamardArgument = new HadamardArgument(commitmentsB, zeroArgument);

		// SingleValueProductArgument.
		final SingleValueProductArgument singleValueProductArgument = new SingleValueProductArgument.Builder().with_c_d(gFour).with_c_delta(gFour)
				.with_c_Delta(gFive).with_a_tilde(GroupVector.of(zTwo, zOne)).with_b_tilde(GroupVector.of(zTwo, zThree)).with_r_tilde(zOne)
				.with_s_tilde(zZero).build();

		// ProductArgument.
		final ProductArgument productArgument = new ProductArgument(gFour, hadamardArgument, singleValueProductArgument);

		// MultiExponentiationArgument
		final ElGamalMultiRecipientCiphertext e0 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gFive, gOne));
		final ElGamalMultiRecipientCiphertext e1 = ElGamalMultiRecipientCiphertext.create(gFour, GroupVector.of(gOne, gFive));
		final ElGamalMultiRecipientCiphertext e2 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gFive, gFive));
		final ElGamalMultiRecipientCiphertext e3 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gThree, gNine));
		final MultiExponentiationArgument multiExponentiationArgument = new MultiExponentiationArgument.Builder().with_c_A_0(gThree)
				.with_c_B(GroupVector.of(gOne, gNine, gOne, gOne)).with_E(GroupVector.of(e0, e1, e2, e3)).with_a(GroupVector.of(zFour, zFour))
				.with_r(zZero).with_b(zFour).with_s(zFour).with_tau(zZero).build();

		// ShuffleArgument.
		return new ShuffleArgument.Builder().with_c_A(GroupVector.of(gNine, gFive)).with_c_B(GroupVector.of(gNine, gFive))
				.with_productArgument(productArgument).with_multiExponentiationArgument(multiExponentiationArgument).build();
	}

	public static ShuffleArgument createSimplestShuffleArgument() {
		// This is an example for m=1,n=2,l=1.

		// SingleValueProductArgument.
		final SingleValueProductArgument singleValueProductArgument = new SingleValueProductArgument.Builder().with_c_d(gFour).with_c_delta(gFour)
				.with_c_Delta(gFive).with_a_tilde(GroupVector.of(zOne, zTwo)).with_b_tilde(GroupVector.of(zZero, zTwo)).with_r_tilde(zZero)
				.with_s_tilde(zZero).build();

		// ProductArgument.
		final ProductArgument productArgument = new ProductArgument(singleValueProductArgument);

		// MultiExponentiationArgument
		final ElGamalMultiRecipientCiphertext e0 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gThree));
		final ElGamalMultiRecipientCiphertext e1 = ElGamalMultiRecipientCiphertext.create(gFour, GroupVector.of(gNine));
		final MultiExponentiationArgument multiExponentiationArgument = new MultiExponentiationArgument.Builder().with_c_A_0(gFour)
				.with_c_B(GroupVector.of(gFive, gFive)).with_E(GroupVector.of(e0, e1)).with_a(GroupVector.of(zThree, zFour)).with_r(zTwo)
				.with_b(zFour).with_s(zFour).with_tau(zZero).build();

		// ShuffleArgument.
		return new ShuffleArgument.Builder().with_c_A(GroupVector.of(gThree)).with_c_B(GroupVector.of(gFour)).with_productArgument(productArgument)
				.with_multiExponentiationArgument(multiExponentiationArgument).build();
	}

	static JsonNode createShuffleArgumentNode() {
		final JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(getShuffleArgumentJson());
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to read ShuffleArgument json.");
		}

		return jsonNode;
	}

	public static String getShuffleArgumentJson() {
		// Return corresponding json to createShuffleArgument.
		return "{\"c_A\":[\"0x9\",\"0x5\"],\"c_B\":[\"0x9\",\"0x5\"],\"productArgument\":{\"c_b\":\"0x4\",\"hadamardArgument\":{\"c_b\":[\"0x9\",\"0x5\"],\"zeroArgument\":{\"c_A_0\":\"0x9\",\"c_B_m\":\"0x5\",\"c_d\":[\"0x5\",\"0x9\",\"0x4\",\"0x1\",\"0x4\"],\"a_prime\":[\"0x3\",\"0x2\"],\"b_prime\":[\"0x4\",\"0x3\"],\"r_prime\":\"0x1\",\"s_prime\":\"0x3\",\"t_prime\":\"0x1\"}},\"singleValueProductArgument\":{\"c_d\":\"0x4\",\"c_delta\":\"0x4\",\"c_Delta\":\"0x5\",\"a_tilde\":[\"0x2\",\"0x1\"],\"b_tilde\":[\"0x2\",\"0x3\"],\"r_tilde\":\"0x1\",\"s_tilde\":\"0x0\"}},\"multiExponentiationArgument\":{\"c_A_0\":\"0x3\",\"c_B\":[\"0x1\",\"0x9\",\"0x1\",\"0x1\"],\"E\":[{\"gamma\":\"0x5\",\"phis\":[\"0x5\",\"0x1\"]},{\"gamma\":\"0x4\",\"phis\":[\"0x1\",\"0x5\"]},{\"gamma\":\"0x5\",\"phis\":[\"0x5\",\"0x5\"]},{\"gamma\":\"0x5\",\"phis\":[\"0x3\",\"0x9\"]}],\"a\":[\"0x4\",\"0x4\"],\"r\":\"0x0\",\"b\":\"0x4\",\"s\":\"0x4\",\"tau\":\"0x0\"}}";
	}

	public static JsonNode createSignatureNode(final CryptoPrimitivesSignature signature) {
		try {
			return mapper.readTree(mapper.writeValueAsString(signature));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize signature.");
		}
	}

	public static ControlComponentCodeSharesPayload getResponsePayload(final String electionEventId, final String verificationCardSetId,
			final int chunkId) {

		final List<ControlComponentCodeShare> controlComponentCodeShares = Arrays
				.asList(getReturnCodeGenerationOutput("1ecb40f5bab5400e8166b63a04a0708d"),
						getReturnCodeGenerationOutput("2ecb40f5bab5400e8166b63a04a0708d"));

		final ControlComponentCodeSharesPayload responsePayload = new ControlComponentCodeSharesPayload(electionEventId,
				verificationCardSetId, chunkId, gqGroup, controlComponentCodeShares, 1);

		// Generate random bytes for signature content and create payload signature.
		final byte[] randomBytes = new byte[10];
		new SecureRandom().nextBytes(randomBytes);
		final CryptoPrimitivesSignature signature = new CryptoPrimitivesSignature(randomBytes);
		responsePayload.setSignature(signature);

		return responsePayload;
	}

	public static ObjectNode createResponsePayloadNode(final ControlComponentCodeSharesPayload responsePayload) throws JsonProcessingException {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("electionEventId", responsePayload.getElectionEventId());
		rootNode.put("verificationCardSetId", responsePayload.getVerificationCardSetId());
		rootNode.put("chunkId", responsePayload.getChunkId());

		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode returnCodeGenerationOutputsNode = mapper.readTree(
				mapper.writer().withAttribute("base64Conversion", true).writeValueAsString(responsePayload.getControlComponentCodeShares()));
		rootNode.set("controlComponentCodeShares", returnCodeGenerationOutputsNode);

		rootNode.put("nodeId", 1);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(responsePayload.getSignature());
		rootNode.set("signature", signatureNode);

		return rootNode;
	}

	public static ControlComponentCodeShare getReturnCodeGenerationOutput(final String verificationCardId) {
		final ElGamalMultiRecipientPublicKey voterChoiceReturnCodeGenerationPublicKey = SerializationTestData.getSingleElementPublicKey();
		final ElGamalMultiRecipientPublicKey voterVoteCastReturnCodeGenerationPublicKey = SerializationTestData.getSingleElementPublicKey();
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedPartialChoiceReturnCodes = SerializationTestData.getCiphertexts(1).get(0);
		final ExponentiationProof encryptedPartialChoiceReturnCodeExponentiationProof = SerializationTestData.createExponentiationProof();
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedConfirmationKey = SerializationTestData.getSinglePhiCiphertext();
		final ExponentiationProof encryptedConfirmationKeyExponentiationProof = SerializationTestData.createExponentiationProof();

		return new ControlComponentCodeShare(verificationCardId, voterChoiceReturnCodeGenerationPublicKey,
				voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
				encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
				encryptedConfirmationKeyExponentiationProof);
	}

	public static SetupComponentVerificationDataPayload getRequestPayload(final Ballot ballot, final String electionEventId,
			final String verificationCardSetId, final int chunkId) {

		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);

		final ElGamalMultiRecipientCiphertext confirmationKey = getSinglePhiCiphertext();
		final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(confirmationKey.getGroup());
		final ElGamalMultiRecipientCiphertext partialChoiceReturnCodes = ElGamalMultiRecipientCiphertext.create(confirmationKey.getGamma(),
				gqGroupGenerator.genRandomGqElementVector(combinedCorrectnessInformation.getTotalNumberOfVotingOptions()));
		final ElGamalMultiRecipientPublicKey verificationCardPublicKey = getSingleElementPublicKey();
		final List<SetupComponentVerificationData> setupComponentVerificationData = Arrays
				.asList(new SetupComponentVerificationData("1ecb40f5bab5400e8166b63a04a0708d", confirmationKey, partialChoiceReturnCodes,
								verificationCardPublicKey),
						new SetupComponentVerificationData("2ecb40f5bab5400e8166b63a04a0708d", confirmationKey, partialChoiceReturnCodes,
								verificationCardPublicKey));

		final List<String> partialChoiceReturnCodesAllowList = IntStream.range(0,
						setupComponentVerificationData.size() * combinedCorrectnessInformation.getTotalNumberOfVotingOptions())
				.mapToObj(String::valueOf)
				.map(value -> value.getBytes(StandardCharsets.UTF_8))
				.map(BaseEncodingFactory.createBase64()::base64Encode)
				.toList();

		final SetupComponentVerificationDataPayload requestPayload = new SetupComponentVerificationDataPayload(electionEventId,
				verificationCardSetId, partialChoiceReturnCodesAllowList, chunkId, gqGroup, setupComponentVerificationData,
				combinedCorrectnessInformation);

		// Generate random bytes for signature content and create payload signature.
		final byte[] randomBytes = new byte[10];
		new SecureRandom().nextBytes(randomBytes);
		final CryptoPrimitivesSignature signature = new CryptoPrimitivesSignature(randomBytes);
		requestPayload.setSignature(signature);

		return requestPayload;
	}

	public static ObjectNode createRequestPayloadNode(final SetupComponentVerificationDataPayload requestPayload) throws JsonProcessingException {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("electionEventId", requestPayload.getElectionEventId());
		rootNode.put("verificationCardSetId", requestPayload.getVerificationCardSetId());

		final JsonNode partialChoiceReturnCodesAllowListNode = mapper.readTree(
				mapper.writeValueAsString(requestPayload.getPartialChoiceReturnCodesAllowList()));
		rootNode.set("partialChoiceReturnCodesAllowList", partialChoiceReturnCodesAllowListNode);

		rootNode.put("chunkId", requestPayload.getChunkId());

		final JsonNode encryptionGroupNode = SerializationTestData.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode returnCodeGenerationInputsNode = mapper.readTree(
				mapper.writer().withAttribute("base64Conversion", true).writeValueAsString(requestPayload.getSetupComponentVerificationData()));
		rootNode.set("setupComponentVerificationData", returnCodeGenerationInputsNode);

		final JsonNode combinedCorrectnessInformationNode = mapper
				.readTree(mapper.writeValueAsString(requestPayload.getCombinedCorrectnessInformation()));
		rootNode.set("combinedCorrectnessInformation", combinedCorrectnessInformationNode);

		final JsonNode signatureNode = SerializationTestData.createSignatureNode(requestPayload.getSignature());
		rootNode.set("signature", signatureNode);

		return rootNode;
	}

	public static ObjectNode createExponentiationProofNode(final ExponentiationProof exponentiationProof) {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("e", bigIntegerToHex(exponentiationProof.get_e().getValue()));
		rootNode.put("z", bigIntegerToHex(exponentiationProof.get_z().getValue()));

		return rootNode;
	}

	public static ObjectNode createExponentiationProofNodeBase64(final ExponentiationProof exponentiationProof) {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("e", bigIntegerToBase64(exponentiationProof.get_e().getValue()));
		rootNode.put("z", bigIntegerToBase64(exponentiationProof.get_z().getValue()));

		return rootNode;
	}
}
