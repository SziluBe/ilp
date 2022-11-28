package uk.ac.ed.inf.Serializer;

public record OutFlightPathEntry(String orderNo, double fromLongitude, double fromLatitude, double angle, double toLongitude, double toLatitude, long ticksSinceStartOfCalculation) {
}
