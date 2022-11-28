package uk.ac.ed.inf.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class OutFlightPathSerializer extends JsonSerializer<OutFlightPath> {
    @Override
    public void serialize(OutFlightPath flightPath, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for (OutFlightPathEntry outFlightPathEntry : flightPath.outFlightPathEntries()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("orderNo", outFlightPathEntry.orderNo());
            jsonGenerator.writeNumberField("fromLongitude", outFlightPathEntry.fromLongitude());
            jsonGenerator.writeNumberField("fromLatitude", outFlightPathEntry.fromLatitude());
            jsonGenerator.writeNumberField("angle", outFlightPathEntry.angle());
            jsonGenerator.writeNumberField("toLongitude", outFlightPathEntry.toLongitude());
            jsonGenerator.writeNumberField("toLatitude", outFlightPathEntry.toLatitude());
            jsonGenerator.writeNumberField("ticksSinceStartOfCalculation", outFlightPathEntry.ticksSinceStartOfCalculation());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }
}
