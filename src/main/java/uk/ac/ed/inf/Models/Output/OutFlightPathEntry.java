package uk.ac.ed.inf.Models.Output;

import uk.ac.ed.inf.Models.LngLat;

public record OutFlightPathEntry(String orderNo,
                                 double fromLongitude,
                                 double fromLatitude,
                                 double angle,
                                 double toLongitude,
                                 double toLatitude,
                                 long ticksSinceStartOfCalculation) {
    public double distance() {
        return new LngLat(fromLongitude, fromLatitude).distanceTo(new LngLat(toLongitude, toLatitude));
    }
}
