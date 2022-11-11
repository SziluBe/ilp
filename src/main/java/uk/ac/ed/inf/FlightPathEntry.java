package uk.ac.ed.inf;

public record FlightPathEntry(String orderNo, double fromLongitude, double fromLatitude, double angle, double toLongitude, double toLatitude, long ticksSinceStartOfCalculation) {
}
