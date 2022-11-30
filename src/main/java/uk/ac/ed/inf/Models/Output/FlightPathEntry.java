package uk.ac.ed.inf.Models.Output;

public record FlightPathEntry(String orderNo,
                              double fromLongitude,
                              double fromLatitude,
                              double angle,
                              double toLongitude,
                              double toLatitude,
                              long ticksSinceStartOfCalculation) {
}
