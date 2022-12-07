package uk.ac.ed.inf.Models.Output;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an entry in the flight path that is output to the user (as a JSON).
 */
public record FlightPathEntry(@NotNull String orderNo,
                              double fromLongitude,
                              double fromLatitude,
                              double angle,
                              double toLongitude,
                              double toLatitude,
                              long ticksSinceStartOfCalculation) {
    /**
     * Default record constructor.
     *
     * @param String orderNo The order number.
     * @param double fromLongitude The longitude of the start of the step.
     * @param double fromLatitude The latitude of the start of the step.
     * @param double angle The angle of the step.
     * @param double toLongitude The longitude of the end of the step.
     * @param double toLatitude The latitude of the end of the step.
     * @param long ticksSinceStartOfCalculation The number of ticks since the start of the calculation.
     */
}
