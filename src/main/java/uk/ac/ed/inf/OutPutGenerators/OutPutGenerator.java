package uk.ac.ed.inf.OutPutGenerators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.DeliveryPlanners.DeliveryPlanner;
import uk.ac.ed.inf.Models.Input.Order;

public interface OutPutGenerator {
    @NotNull
    static OutPutGenerator getOutPutGenerator(@NotNull DeliveryPlanner deliveryPlanner) {
        var objectMapper = new ObjectMapper();
        return new JsonOutPutGenerator(deliveryPlanner, objectMapper);
    }

    String generateFlightPathOutPut(@NotNull Order[] deliveredOrders, @NotNull String date);

    String generateFlightPathMapOutPut(@NotNull Order[] deliveredOrders, @NotNull String date);

    String generateDeliveriesOutPut(@NotNull Order[] orders, @NotNull String date);
}
