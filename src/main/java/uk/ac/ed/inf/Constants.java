package uk.ac.ed.inf;

import uk.ac.ed.inf.Models.Direction;
import uk.ac.ed.inf.Models.LngLat;

public final class Constants {
    public static final double CLOSETO_DIST = 0.00015;
    public static final double MOVE_LENGTH = 0.00015;
    public static final int DELIVERY_CHARGE = 100;
    public static final int MAX_MOVES = 2000;
    public static final LngLat AT = new LngLat(-3.186874, 55.944494);

    public static final Direction HOVER = null;

    private Constants() {
    }
}
