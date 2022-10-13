package uk.ac.ed.inf;

import java.net.URL;

public final class Constants {
    private Constants() {}

    public static final double CLOSETO_DIST = 0.00015;
    public static final URL DEFAULT_BASE_ADDRESS = new URL("https://ilp-rest.azurewebsites.net/"); // We know the URL is not malformed, as it has been hard-coded and tested by hand, therefore there is no need for a try-catch
}
