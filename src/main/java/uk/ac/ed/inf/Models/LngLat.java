package uk.ac.ed.inf.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Models.Input.Area;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LngLat(double lng, double lat) {
    /**
     * Con
     */

    public static final double CLOSETO_DIST = 0.00015;
    public static final double DOUBLE_EPSILON = 0.0000001;

    /**
     * Represents a point on the Earth's surface.
     *
     * @param lng The longitude of the point
     * @param lat The latitude of the point
     */

    @JsonCreator
    public LngLat(@com.fasterxml.jackson.annotation.JsonProperty("longitude") double lng, @com.fasterxml.jackson.annotation.JsonProperty("latitude") double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    /**
     * Checks whether the point represented by the LngLat instance is within the given Area or not, including boundary points.
     *
     * @return Return true if the point is inside, false otherwise
     */
    public boolean inArea(Area area) { // from https://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon
        int i;
        int j;
        boolean result = false;
        LngLat[] points = area.getVertices();
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].lat() > this.lat()) != (points[j].lat() > this.lat()) &&
                    (this.lng() < (points[j].lng() - points[i].lng()) * (this.lat() - points[i].lat()) / (points[j].lat() - points[i].lat()) + points[i].lng())) {
                result = !result;
            }
        }
        // Our method so far returns false for points on the boundary, so we add the following
        for (i = 0; i < points.length - 1; i++) {
            // using the triangle inequality to determine if the point lies on this edge
            if (Math.abs(this.distanceTo(points[i]) + this.distanceTo(points[i + 1]) - points[i].distanceTo(points[i + 1])) < DOUBLE_EPSILON) {
                return true;
            }
        }

        return result;
    }

    /**
     * Calculates the Euclidean distance between the point represented by this LngLat instance, and the point represented by the LngLat instance passed in as a parameter.
     *
     * @param target The LngLat instance representing the point we want to calculate the distance to
     * @return The distance between the two points
     */
    public double distanceTo(LngLat target) {
        double lngSquared = Math.pow(target.lng() - this.lng(), 2);
        double latSquared = Math.pow(target.lat() - this.lat(), 2);

        return Math.sqrt(lngSquared + latSquared);
    }

    /**
     * Tells us whether the point represented by this LngLat instance is 'close to' the point represented by the LngLat instance passed in as a parameter.
     * By 'close to', we mean the distance between the 2 points is less than 0.00015 in the longitude/latitude coordinate units we are using.
     *
     * @param target The LngLat instance representing the point we want to check the closeness off.
     * @return Return true if the 2 points are close to each other, false otherwise.
     */
    public boolean closeTo(LngLat target) {
        return this.distanceTo(target) < CLOSETO_DIST;
    }

    /**
     * Calculates the position the drone would be at after moving 1 unit (of length 0.00015) in the compass direction passed in as a parameter.
     *
     * @param dir The CmpDir object representing the compass direction in which the hypothetical move is happening,
     *            or null if the drone is doing a hover move.
     * @return Returns the LngLat object representing the point the drone would end up at after taking this move.
     */
    @NotNull
    public LngLat nextPosition(Direction dir) {
        if (dir == Direction.HOVER) {
            return this;
        }
        return add(dir.toLngLat());
    }

    @NotNull
    private LngLat add(@NotNull LngLat otherLngLat) {
        return new LngLat(this.lng() + otherLngLat.lng(), this.lat() + otherLngLat.lat());
    }
}
