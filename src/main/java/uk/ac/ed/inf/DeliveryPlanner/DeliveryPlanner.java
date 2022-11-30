package uk.ac.ed.inf.DeliveryPlanner;

import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.OrderOutcome;

import java.util.List;

public interface DeliveryPlanner {
    List<Step> getPathForOrder(Order order);
    OrderOutcome getOrderOutcome(Order order);
    Order[] getDeliveredOrders();
    Order[] getValidUndeliveredOrders();
    Order[] getInvalidOrders();
}
