package uk.ac.ed.inf.OutputGenerators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.DeliveryPlanners.DeliveryPlanner;
import uk.ac.ed.inf.Models.Input.Order;

/**
 * Represents an output generator.
 */
public interface OutputGenerator {
    /**
     * Gets an instance of the appropriate kind of output generator for the given delivery planner.
     *
     * @param deliveryPlanner The delivery planner.
     * @return The appropriate output generator.
     */
    @NotNull
    static OutputGenerator getOutputGenerator(@NotNull DeliveryPlanner deliveryPlanner) {
        var objectMapper = new ObjectMapper();
        return new JsonOutputGenerator(deliveryPlanner, objectMapper);
    }

    /**
     * Generates the output that gives each step of the flightpath for the given date.
     *
     * @param deliveredOrders The orders that were delivered on the given date.
     * @param date The date.
     * @return The output, in a format specified by the implementation.
     */
    String generateFlightPathOutput(@NotNull Order[] deliveredOrders, @NotNull String date);

    /**
     * Generates output that can be used to visualise the flightpath for the given date on a map.
     *
     * @param deliveredOrders The orders that were delivered on the given date.
     * @param date The date.
     * @return The output, in a format specified by the implementation.
     */
    String generateFlightPathMapOutput(@NotNull Order[] deliveredOrders, @NotNull String date);

    /**
     * Generates the output that gives the details of the delivery for each order for the given date.
     *
     * @param orders The orders for the given date.
     * @param date The date.
     * @return The output, in a format specified by the implementation.
     */
    String generateDeliveriesOutput(@NotNull Order[] orders, @NotNull String date);
}
