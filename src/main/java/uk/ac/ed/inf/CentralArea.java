package uk.ac.ed.inf;

import java.net.URL;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public enum CentralArea {
    CENTRAL_AREA(new LngLat[] {new LngLat(-3.192473, 55.946233),   // Forrest Hill
                               new LngLat(-3.184319, 55.946233),   // KFC
                               new LngLat(-3.192473, 55.942617),   // Top of the Meadows
                               new LngLat(-3.184319, 55.942617)},  // Buccleuch St Bus Stop
                 null);

    private LngLat[] corners;
    private URL serverBaseAddress;

    public LngLat[] getCorners() { return this.corners; }

    CentralArea(LngLat[] corners, URL serverBaseAddress){
        this.corners = corners;
        this.serverBaseAddress = serverBaseAddress;
    }

    /**
     * Gets the Singleton instance of CentralArea, fetches the relevant data and constructs the instance if not yet present in memory.
     * @param serverBaseAddress     The base address of the REST service from which to fetch the data for the CentralArea instance.
     * @return                      The Singleton instance of CentralArea representing the Central Campus area described in the specs.
     * @throws IOException          Throws an IOException if there are any issues retrieving the data.
     */
    public static CentralArea getInstance(URL serverBaseAddress) throws IOException {
        if (serverBaseAddress == null){
            serverBaseAddress = Constants.DEFAULT_BASE_ADDRESS;
        }

        if (CENTRAL_AREA.serverBaseAddress == null){
            CENTRAL_AREA.corners = CentralArea.fetchCorners(serverBaseAddress);
            CENTRAL_AREA.serverBaseAddress = serverBaseAddress;
        }

        return CENTRAL_AREA;
    }

    private static LngLat[] fetchCorners(URL serverBaseAddress) throws IOException {
        final NamedLocation[] cornerNamedLocations = new ObjectMapper().readValue(new URL(serverBaseAddress + "centralArea/"), NamedLocation[].class);

        LngLat[] corners = new LngLat[cornerNamedLocations.length];

        for (int i = 0; i < cornerNamedLocations.length; i++){
            corners[i] = new LngLat(cornerNamedLocations[i].longitude(), cornerNamedLocations[i].latitude());
        }

        return corners;
    }
}
