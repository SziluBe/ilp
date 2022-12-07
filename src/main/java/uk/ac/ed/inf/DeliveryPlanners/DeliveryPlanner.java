package uk.ac.ed.inf.DeliveryPlanners;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.PathFinders.PathFinder;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.util.List;

public interface DeliveryPlanner {
    int MAX_MOVES = 2000;

    @NotNull
    static DeliveryPlanner getDeliveryPlanner(@NotNull ApplicationData appData, @NotNull PathFinder pathFinder) {
        return new DefaultDeliveryPlanner(appData, pathFinder);
    }

    @Nullable
    List<Step> getPathForOrder(Order order);

    @Nullable
    OrderOutcome getOrderOutcome(Order order);

    @NotNull
    Order[] getDeliveredOrders();

    @NotNull
    Order[] getValidUndeliveredOrders();

    @NotNull
    Order[] getInvalidOrders();
}
