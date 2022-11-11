package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public record Area(LngLat[] vertices) {
    public LngLat[] getVertices() {
        return vertices;
    }

    static Area[] getNoFlyZonesFromRestServer(URL serverBaseAddress) throws IOException {
        return Arrays.stream((new ObjectMapper().readValue(new URL(serverBaseAddress + "noFlyZones"), NamedArea[].class))).map(NamedArea::getArea).toArray(Area[]::new);
    }

    static Area getCentralAreaFromRestServer(URL serverBaseAddress) throws IOException {
        NamedLocation[] centralAreaVerticesOriginal = new ObjectMapper().readValue(new URL(serverBaseAddress + "centralArea"), NamedLocation[].class);
        LngLat[] centralAreaVertices = new LngLat[centralAreaVerticesOriginal.length];
        for (int i = 0; i < centralAreaVerticesOriginal.length; i++) {
            centralAreaVertices[i] = centralAreaVerticesOriginal[i].getLocation();
        }
        return new Area(centralAreaVertices);
    }
}
