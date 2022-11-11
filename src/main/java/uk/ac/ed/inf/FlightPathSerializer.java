package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class FlightPathSerializer extends JsonSerializer<FlightPath> {
    @Override
    public void serialize(FlightPath flightPath, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for (FlightPathEntry flightPathEntry : flightPath.flightPathEntries()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("orderNo", flightPathEntry.orderNo());
            jsonGenerator.writeNumberField("fromLongitude", flightPathEntry.fromLongitude());
            jsonGenerator.writeNumberField("fromLatitude", flightPathEntry.fromLatitude());
            jsonGenerator.writeNumberField("angle", flightPathEntry.angle());
            jsonGenerator.writeNumberField("toLongitude", flightPathEntry.toLongitude());
            jsonGenerator.writeNumberField("toLatitude", flightPathEntry.toLatitude());
            jsonGenerator.writeNumberField("ticksSinceStartOfCalculation", flightPathEntry.ticksSinceStartOfCalculation());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }
}
