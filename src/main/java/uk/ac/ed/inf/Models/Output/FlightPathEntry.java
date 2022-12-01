package uk.ac.ed.inf.Models.Output;

import org.jetbrains.annotations.NotNull;

public record FlightPathEntry(@NotNull String orderNo,
                              double fromLongitude,
                              double fromLatitude,
                              double angle,
                              double toLongitude,
                              double toLatitude,
                              long ticksSinceStartOfCalculation) {
}
