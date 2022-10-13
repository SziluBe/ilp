package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;

public final class Constants {
    private Constants() {}
    public static final double CLOSETO_DIST = 0.00015;
    public static final int DELIVERY_CHARGE = 100;
    public static final URL DEFAULT_BASE_ADDRESS;

    static {
        try {
            DEFAULT_BASE_ADDRESS = new URL("https://ilp-rest.azurewebsites.net/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
