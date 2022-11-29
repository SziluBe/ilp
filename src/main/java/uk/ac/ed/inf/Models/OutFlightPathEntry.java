package uk.ac.ed.inf.Models;

public record OutFlightPathEntry(String orderNo, double fromLongitude, double fromLatitude, double angle, double toLongitude, double toLatitude, long ticksSinceStartOfCalculation) {
}
