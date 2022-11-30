package uk.ac.ed.inf.OutPutGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.DeliveryPlanner.DeliveryPlanner;

public class OutPutGeneratorFactory {
    public static OutPutGenerator getOutPutGenerator(DeliveryPlanner deliveryPlanner) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new DefaultOutPutGenerator(deliveryPlanner, objectMapper);
    }
}
