package uk.ac.ed.inf;

import java.lang.Math;

public record LngLat(double lng, double lat){
    public boolean inCentralArea() { // from https://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon
        int i;
        int j;
        boolean result = false;
        points = CentralArea.CENTRAL_AREA.getCorners();
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > test.y) != (points[j].y > test.y) &&
                    (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y-points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }

    public double distanceTo(LngLat target){
        double lngSquared = Math.pow(target.lng() - this.lng(), 2);
        double latSquared = Math.pow(target.lat() - this.lat(), 2);

        return Math.sqrt(lngSquared + latSquared);
    }

    public boolean closeTo(LngLat target){
        return this.distanceTo(target) < Constants.CLOSETO_DIST;
    }

    public LngLat nextPosition(CmpDir dir){
        return add(dir.toLngLat());
    }

    private LngLat add(LngLat otherLngLat){
        double lngDelta = otherLngLat.lng();
        double latDelta = otherLngLat.lat();

        return new LngLat(this.lng() + otherLngLat.lng(), this.lat() + otherLngLat.lat());
    }
}
