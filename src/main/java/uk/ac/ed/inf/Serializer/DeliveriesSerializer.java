package uk.ac.ed.inf.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class DeliveriesSerializer extends JsonSerializer<Deliveries> {
    @Override
    public void serialize(Deliveries deliveries, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for (DeliveryEntry deliveryEntry : deliveries.getDeliveryEntries()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("orderNo", deliveryEntry.orderNo());
            jsonGenerator.writeStringField("outcome", deliveryEntry.outcome().toString());
            jsonGenerator.writeNumberField("costInPence", deliveryEntry.costInPence());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }
}
