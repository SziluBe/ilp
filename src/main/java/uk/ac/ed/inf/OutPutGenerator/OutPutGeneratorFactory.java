package uk.ac.ed.inf.OutPutGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.DeliveryPlanner.DeliveryPlanner;

public class OutPutGeneratorFactory {
    @NotNull
    public static OutPutGenerator getOutPutGenerator(@NotNull DeliveryPlanner deliveryPlanner) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new DefaultOutPutGenerator(deliveryPlanner, objectMapper);
    }
}
