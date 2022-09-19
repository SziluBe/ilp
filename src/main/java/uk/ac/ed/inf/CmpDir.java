package uk.ac.ed.inf;

import java.lang.Math;

public enum CmpDir {
    E   (0*22.5),
    ENE (1*22.5),
    NE  (2*22.5),
    NNE (3*22.5),
    N   (4*22.5),
    NNW (5*22.5),
    NW  (6*22.5),
    WNW (7*22.5),
    W   (8*22.5),
    WSW (9*22.5),
    SW  (10*22.5),
    SSW (11*22.5),
    S   (12*22.5),
    SSE (13*22.5),
    SE  (14*22.5),
    ESE (15*22.5);

    private final double angle;

    CmpDir(double angle){
        this.angle = angle;
    }

    public LngLat toLngLat(){
        double lng = Math.cos(Math.toRadians(this.angle));
        double lat = Math.sin(Math.toRadians(this.angle));

        return new LngLat(lng, lat);
    }
}
