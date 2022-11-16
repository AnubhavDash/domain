package ch.post.it.evoting.cryptoprimitives.domain.mapper;

import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class EncryptionGroupUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionGroupUtils.class);

	private static final Map<String, GqGroup> ENCRYPTION_GROUPS = new ConcurrentHashMap<>();

	private EncryptionGroupUtils() {
	  //intentionally left blank
    }

	public static GqGroup getEncryptionGroup(final ObjectMapper mapper, final JsonNode encryptionGroupNode) {

		final String key = encryptionGroupNode.get("p").asText();

		return ENCRYPTION_GROUPS.computeIfAbsent(key, k -> {
			try {
				LOGGER.info("Instantiation of a new GqGroup. [p: {}]", key);
				return mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);
			} catch (final JsonProcessingException e) {
				throw new UncheckedIOException("Failed to deserialize encryption group.", e);
			}
		});
	}
}
