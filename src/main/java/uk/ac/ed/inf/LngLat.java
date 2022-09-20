package uk.ac.ed.inf;

import java.lang.Math;

public record LngLat(double lng, double lat){
//    public boolean inCentralArea() {
//        // TODO
//    }

    public double distanceTo(LngLat target){
        double lngSquared = Math.pow(target.lng() - this.lng(), 2);
        double latSquared = Math.pow(target.lat() - this.lat(), 2);

        return Math.sqrt(lngSquared + latSquared);
    }

    public boolean closeTo(LngLat target){
        // TODO: maybe store constants somewhere better
        double CLOSETO_DIST = 0.00015;

        return this.distanceTo(target) < CLOSETO_DIST;
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
