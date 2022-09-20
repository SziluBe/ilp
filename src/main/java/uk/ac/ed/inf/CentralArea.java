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

    CentralArea(LngLat[] corners, URL serverBaseAddress){
        this.corners = corners;
        this.serverBaseAddress = serverBaseAddress;
    }

    public static CentralArea getInstance(URL serverBaseAddress){
        if (serverBaseAddress == null){
            // TODO
            throw new RuntimeException();
        }

        if (CENTRAL_AREA.serverBaseAddress == null){
            CENTRAL_AREA.corners = CentralArea.getCorners(serverBaseAddress);
            CENTRAL_AREA.serverBaseAddress = serverBaseAddress;
        } else if (serverBaseAddress != CENTRAL_AREA.serverBaseAddress) {
            // TODO
            throw new RuntimeException();
        }

        return CENTRAL_AREA;
    }

    private static LngLat[] getCorners(URL serverBaseAddress){
        try {
            final Location[] cornerLocations = new ObjectMapper().readValue(new URL(serverBaseAddress + "centralArea/"), Location[].class);

            LngLat[] corners = new LngLat[cornerLocations.length];

            for (int i = 0; i < cornerLocations.length; i++){
                corners[i] = new LngLat(cornerLocations[i].longitude(), cornerLocations[i].latitude());
            }

            return corners;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
