package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.Models.LngLat;

import java.net.MalformedURLException;
import java.net.URL;

public final class Constants {
    public static final double CLOSETO_DIST = 0.00015;
    public static final double MOVE_LENGTH = 0.00015;
    public static final int DELIVERY_CHARGE = 100;
    public static final int MAX_MOVES = 2000;
    public static final URL DEFAULT_BASE_ADDRESS;
    public static final LngLat AT = new LngLat(-3.186874, 55.944494);

    public static final Double HOVER_ANGLE = null;

    static {
        try {
            DEFAULT_BASE_ADDRESS = new URL("https://ilp-rest.azurewebsites.net/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Constants() {
    }
}
