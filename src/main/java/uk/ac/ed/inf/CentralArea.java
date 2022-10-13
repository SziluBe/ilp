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
        final Location[] cornerLocations = new ObjectMapper().readValue(new URL(serverBaseAddress + "centralArea/"), Location[].class);

        LngLat[] corners = new LngLat[cornerLocations.length];

        for (int i = 0; i < cornerLocations.length; i++){
            corners[i] = new LngLat(cornerLocations[i].longitude(), cornerLocations[i].latitude());
        }

        return corners;
    }
}
