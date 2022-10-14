package uk.ac.ed.inf;

public record LngLat(double lng, double lat) {
    /**
     * Checks whether the point represented by the LngLat instance is within the Central Area or not.
     *
     * @return Return true if the point is inside, false otherwise
     */
    public boolean inCentralArea() { // from https://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon
        int i;
        int j;
        boolean result = false;
        LngLat[] points = CentralArea.CENTRAL_AREA.getCorners();
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].lat() > this.lat()) != (points[j].lat() > this.lat()) && (this.lng() < (points[j].lng() - points[i].lng()) * (this.lat() - points[i].lat()) / (points[j].lat() - points[i].lat()) + points[i].lng())) {
                result = !result;
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
        return this.distanceTo(target) < Constants.CLOSETO_DIST;
    }

    /**
     * Calculates the position the drone would be at after moving 1 unit (of length 0.00015) in the compass direction passed in as a parameter.
     *
     * @param dir The CmpDir object representing the compass direction in which the hypothetical move is happening.
     * @return Returns the LngLat object representing the point the drone would end up at after taking this move.
     */
    public LngLat nextPosition(CmpDir dir) {
        return add(dir.toLngLat());
    }

    private LngLat add(LngLat otherLngLat) {
        double lngDelta = otherLngLat.lng();
        double latDelta = otherLngLat.lat();

        return new LngLat(this.lng() + otherLngLat.lng(), this.lat() + otherLngLat.lat());
    }
}
