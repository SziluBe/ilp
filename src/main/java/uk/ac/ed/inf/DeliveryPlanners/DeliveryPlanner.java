package uk.ac.ed.inf.DeliveryPlanners;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.PathFinders.PathFinder;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.util.List;

/**
 * Represents a delivery planner.
 */
public interface DeliveryPlanner {
    /**
     * The maximum number of moves the drone can make in a single day (before it needs to be recharged).
     */
    int MAX_MOVES = 2000;

    /**
     * Gets an instance of the appropriate kind of delivery planner for the given combination of application data and pathfinder.
     *
     * @param appData    The application data.
     * @param pathFinder The pathfinder.
     * @return The appropriate delivery planner.
     */
    @NotNull
    static DeliveryPlanner getDeliveryPlanner(@NotNull ApplicationData appData, @NotNull PathFinder pathFinder) {
        return new DefaultDeliveryPlanner(appData, pathFinder);
    }

    /**
     * Gets the flight path to the restaurant for the given order.
     *
     * @param order The order.
     * @return The flight path for the order.
     */
    @Nullable
    List<Step> getPathForOrder(Order order);

    /**
     * Gets the outcome of the given order.
     *
     * @param order The order.
     * @return The outcome of the order.
     */
    @Nullable
    OrderOutcome getOrderOutcome(Order order);

    /**
     * Gets an array that contains the orders that were delivered on the given date (from appData).
     *
     * @return The orders that were delivered on the given date.
     */
    @NotNull
    Order[] getDeliveredOrders();

    /**
     * Gets an array that contains the orders that are valid but were not delivered on the given date (from appData).
     *
     * @return The orders that are valid but were not delivered on the given date.
     */
    @NotNull
    Order[] getValidUndeliveredOrders();

    /**
     * Gets an array that contains the orders on the given date (from appData) that are invalid.
     *
     * @return The orders on the given date that are invalid.
     */
    @NotNull
    Order[] getInvalidOrders();
}
