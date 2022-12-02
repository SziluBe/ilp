package uk.ac.ed.inf.OutputGenerators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.DeliveryPlanners.DeliveryPlanner;
import uk.ac.ed.inf.Models.Input.Order;

public interface OutputGenerator {
    @NotNull
    static OutputGenerator getOutputGenerator(@NotNull DeliveryPlanner deliveryPlanner) {
        var objectMapper = new ObjectMapper();
        return new JsonOutputGenerator(deliveryPlanner, objectMapper);
    }

    String generateFlightPathOutput(@NotNull Order[] deliveredOrders, @NotNull String date);

    String generateFlightPathMapOutput(@NotNull Order[] deliveredOrders, @NotNull String date);

    String generateDeliveriesOutput(@NotNull Order[] orders, @NotNull String date);
}
