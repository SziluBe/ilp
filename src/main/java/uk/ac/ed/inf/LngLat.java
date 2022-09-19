package uk.ac.ed.inf;

import java.lang.Math;

public record LngLat(double lng, double lat){
    public double getLng(){
        return this.lng;
    }

    public double getLat(){
        return this.lat;
    }

    // TODO
    public boolean inCentralArea() {
        return false;
    }

    public double distanceTo(LngLat target){
        double lngSquared = Math.pow(target.lng - this.lng, 2);
        double latSquared = Math.pow(target.lat - this.lat, 2);
        return Math.sqrt(lngSquared + latSquared);
    }

    public boolean closeTo(LngLat target){
        double CLOSETO_DIST = 0.00015;

        return this.distanceTo(target) < CLOSETO_DIST;
    }

    public LngLat nextPosition(CmpDir dir){
        return add(dir.toLngLat());
    }

    private LngLat add(LngLat otherLngLat){
        double lngDelta = otherLngLat.getLng();
        double latDelta = otherLngLat.getLat();

        return new LngLat(this.lng + otherLngLat.getLng(), this.lat + otherLngLat.getLat());
    }
}
