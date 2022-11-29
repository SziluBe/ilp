package uk.ac.ed.inf.Models;

import uk.ac.ed.inf.Constants;

public enum Direction {
    E(0 * 22.5),
    ENE(1 * 22.5),
    NE(2 * 22.5),
    NNE(3 * 22.5),
    N(4 * 22.5),
    NNW(5 * 22.5),
    NW(6 * 22.5),
    WNW(7 * 22.5),
    W(8 * 22.5),
    WSW(9 * 22.5),
    SW(10 * 22.5),
    SSW(11 * 22.5),
    S(12 * 22.5),
    SSE(13 * 22.5),
    SE(14 * 22.5),
    ESE(15 * 22.5),
    HOVER(Constants.HOVER_ANGLE);

    private final Double angle;

    Direction(Double angle) {
        this.angle = angle;
    }

    public Double getAngle() {
        return angle;
    }

    /**
     * Returns a LngLat object whose coordinates when summed together with another LngLat object's coordinates would be equal to the coordinates we would
     * end up at after moving from that other LngLat object's coordinates in the compass direction represented by this CmpDir instance.
     *
     * @return The LngLat object representing the change in coordinates after moving in the direction represented by this CmpDir instance.
     */
    public LngLat toLngLat() {
        if (this == HOVER) {
            return new LngLat(0, 0);
        }

        double lng = Math.cos(Math.toRadians(this.angle)) * Constants.MOVE_LENGTH;
        double lat = Math.sin(Math.toRadians(this.angle)) * Constants.MOVE_LENGTH;

        return new LngLat(lng, lat);
    }
}
