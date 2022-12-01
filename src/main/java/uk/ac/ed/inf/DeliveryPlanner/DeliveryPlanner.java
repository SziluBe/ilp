package uk.ac.ed.inf.DeliveryPlanner;

import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.OrderOutcome;

import java.util.List;

public interface DeliveryPlanner {
    @Nullable
    List<Step> getPathForOrder(Order order);
    @Nullable
    OrderOutcome getOrderOutcome(Order order);
    Order[] getDeliveredOrders();
    Order[] getValidUndeliveredOrders();
    Order[] getInvalidOrders();
}
