package ch.post.it.evoting.cryptoprimitives.domain.mixnet;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ch.post.it.evoting.cryptoprimitives.math.PrimeGqElement;

public class PrimeGroupElementSerializer extends JsonSerializer<PrimeGqElement> {
	@Override
	public void serialize(PrimeGqElement value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeNumber(value.getValueAsInt());
	}
}
