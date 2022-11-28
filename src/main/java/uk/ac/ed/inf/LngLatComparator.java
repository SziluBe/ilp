package uk.ac.ed.inf;

import uk.ac.ed.inf.Models.LngLat;

public class LngLatComparator {
    private LngLat start;
    private LngLat end;

    public LngLatComparator(LngLat start, LngLat end) {
        this.start = start;
        this.end = end;
    }

    public int compare(LngLat a, LngLat b) {
        double distanceA = start.distanceTo(a) + a.distanceTo(end);
        double distanceB = start.distanceTo(b) + b.distanceTo(end);
        if (distanceA < distanceB) {
            return -1;
        } else if (distanceA > distanceB) {
            return 1;
        } else {
            return 0;
        }
    }
}
