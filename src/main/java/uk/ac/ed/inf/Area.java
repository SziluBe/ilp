package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public record Area(LngLat[] vertices) {
    public LngLat[] getVertices() {
        return vertices;
    }

    private static Area centralArea;

    private static Area[] noFlyZones;

    private static URL centralAreaBaseAddress;
    private static URL noFlyZonesBaseAddress;

    private static Area[] getNoFlyZonesFromRestServer(URL serverBaseAddress) throws IOException {
        return Arrays.stream((Constants.MAPPER.readValue(new URL(serverBaseAddress + "noFlyZones"), NamedArea[].class))).map(NamedArea::getArea).toArray(Area[]::new);
    }

    public static Area[] getNoFlyZones(URL serverBaseAddress) throws IOException {
        if (noFlyZones == null) {
            try {
                noFlyZones = getNoFlyZonesFromRestServer(serverBaseAddress);
                noFlyZonesBaseAddress = serverBaseAddress;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (!serverBaseAddress.equals(noFlyZonesBaseAddress)) {
            // TODO: is err the right place to print this?
            System.err.println("Warning: noFlyZones base address has changed from " + noFlyZonesBaseAddress + " to " + serverBaseAddress + ", but noFlyZones have already been fetched. This may cause unexpected behaviour.");
        }
        return noFlyZones;
    }

    private static Area getCentralAreaFromRestServer(URL serverBaseAddress) throws IOException {
        NamedLocation[] centralAreaVerticesOriginal = Constants.MAPPER.readValue(new URL(serverBaseAddress + "centralArea"), NamedLocation[].class);
        LngLat[] centralAreaVertices = new LngLat[centralAreaVerticesOriginal.length];
        for (int i = 0; i < centralAreaVerticesOriginal.length; i++) {
            centralAreaVertices[i] = centralAreaVerticesOriginal[i].getLocation();
        }
        return new Area(centralAreaVertices);
    }

    public static Area getCentralArea(URL serverBaseAddress) {
        if (centralArea == null) {
            try {
                centralArea = getCentralAreaFromRestServer(serverBaseAddress);
                centralAreaBaseAddress = serverBaseAddress;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (!serverBaseAddress.equals(centralAreaBaseAddress)) {
            // TODO: is err the right place to print this?
            System.err.println("Warning: Central area base address has changed from " + centralAreaBaseAddress + " to " + serverBaseAddress + ", but centralArea has already been fetched. This may cause unexpected behaviour.");
        }
        return centralArea;
    }
}
