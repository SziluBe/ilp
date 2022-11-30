package uk.ac.ed.inf.OutPutGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.ac.ed.inf.Models.Input.Order;

public interface OutPutGenerator {
    String generateFlightPathOutPut(Order[] deliveredOrders) throws JsonProcessingException;
    String generateFlightPathGeoJsonOutPut() throws JsonProcessingException;
    String generateDeliveriesOutPut(Order[] orders) throws JsonProcessingException;
}
