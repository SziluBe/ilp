package uk.ac.ed.inf.DeliveryPlanners;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Models.Input.MenuItem;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Input.Restaurant;
import uk.ac.ed.inf.Models.Output.OrderOutcome;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.PathFinders.PathFinder;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Represents an instance of the default delivery planner.
 * The default delivery planner uses a pathfinder to find the shortest path for each order, and
 * then plans the flights to deliver as many orders as possible within the limit of the drone's
 * battery.
 */
public class DefaultDeliveryPlanner implements DeliveryPlanner {

    private final PathFinder flightpathFinder;
    private final ApplicationData appData;
    private final Map<Order, Restaurant> orderToRestaurantMap = new HashMap<>();
    private final Map<Order, Integer> orderToRequiredStepsMap = new HashMap<>();
    private final Map<Order, OrderOutcome> orderToOutcomeMap = new HashMap<>();
    private boolean isRestaurantMapCalculated = false;
    private boolean isRequiredStepsMapCalculated = false;
    private boolean isOutcomeMapCalculated = false;

    /**
     * Creates a new instance of the default delivery planner.
     *
     * @param appData          The application data.
     * @param flightpathFinder The pathfinder.
     */
    public DefaultDeliveryPlanner(@NotNull ApplicationData appData, @NotNull PathFinder flightpathFinder) {
        this.appData = appData;
        this.flightpathFinder = flightpathFinder;
    }

    private void assignRestaurantsToOrders() {
        // ensure we only run calculations once
        if (isRestaurantMapCalculated) {
            return;
        }

        for (var order : appData.orders()) {
            for (var restaurant : appData.restaurants()) {
                if (Arrays.stream(restaurant.menuItems())
                        .anyMatch(menuItem -> menuItem.name().equals(order.orderItems()[0]))) {
                    orderToRestaurantMap.put(order, restaurant);
                    break;
                }
            }
        }
        isRestaurantMapCalculated = true;
    }

    @Nullable
    private Restaurant getRestaurantForOrder(Order order) {
        // ensure all orders have a restaurant assigned
        assignRestaurantsToOrders();
        return orderToRestaurantMap.get(order);
    }

    private void assignRequiredStepsToOrders() {
        // ensure we only run calculations once
        if (isRequiredStepsMapCalculated) {
            return;
        }
        System.out.println("Calculating required steps for orders: " + appData.orders()[0].orderDate());

        for (var order : appData.orders()) {
            var restaurant = getRestaurantForOrder(order);
            if (restaurant == null) {
                continue;
            }
            List<Step> flightPathEntries = flightpathFinder.getFlightPath(restaurant);
            if (flightPathEntries != null) {
                orderToRequiredStepsMap.put(
                        order,
                        flightPathEntries.size()
                );
            }
        }
        isRequiredStepsMapCalculated = true;
    }

    @Nullable
    private Integer getRequiredStepsForOrder(Order order) {
        // ensure all orders have a restaurant assigned
        assignRequiredStepsToOrders();
        return orderToRequiredStepsMap.get(order);
    }

    private void assignOutcomesToOrders() {
        // ensure we only run calculations once
        if (isOutcomeMapCalculated) {
            return;
        }

        var menuItems = Arrays.stream(appData.restaurants())
                .flatMap(restaurant -> Arrays.stream(restaurant.menuItems()))
                .toArray(MenuItem[]::new);

        // validate orders
        for (var order : appData.orders()) {
            var restaurant = getRestaurantForOrder(order);
            orderToOutcomeMap.put(
                    order,
                    order.validateOrder(restaurant, menuItems, appData.date())
            );
        }

        var deliverableOrders = Arrays.stream(appData.orders())
                .filter(order -> getRequiredStepsForOrder(order) != null)
                .sorted(Comparator.comparingInt(this::getRequiredStepsForOrder)) // cannot be null despite IntelliJ warning; we check in filter
                .toArray(Order[]::new);
        // sort orders by steps
        // calculate final outcomes
        int nSteps = 0;
        for (var order : deliverableOrders) {
            if (orderToOutcomeMap.get(order) == OrderOutcome.ValidButNotDelivered) {
                Integer addedSteps = getRequiredStepsForOrder(order);
                if (addedSteps != null) {
                    int newSteps = nSteps + addedSteps;
                    if (newSteps <= DeliveryPlanner.MAX_MOVES) {
                        orderToOutcomeMap.put(order, OrderOutcome.Delivered);
                        nSteps = newSteps;
                    }
                }
            }
        }
        isOutcomeMapCalculated = true;
    }

    /**
     * @inheritDoc
     */
    @Nullable
    public List<Step> getPathForOrder(Order order) {
        var restaurant = getRestaurantForOrder(order);
        if (restaurant == null) {
            return null;
        }

        return flightpathFinder.getFlightPath(restaurant);
    }

    /**
     * @inheritDoc
     */
    @Nullable
    public OrderOutcome getOrderOutcome(Order order) {
        // ensure orders have their outcomes assigned
        assignOutcomesToOrders();
        return orderToOutcomeMap.get(order);
    }

    /**
     * @inheritDoc
     */
    @NotNull
    public Order[] getDeliveredOrders() {
        // will never be null as empty streams still return an array on toArray
        return Arrays.stream(appData.orders())
                .filter(order -> getOrderOutcome(order) == OrderOutcome.Delivered)
                .toArray(Order[]::new);
    }

    /**
     * @inheritDoc
     */
    @NotNull
    public Order[] getValidUndeliveredOrders() {
        // will never be null as empty streams still return an array on toArray
        return Arrays.stream(appData.orders())
                .filter(order -> getOrderOutcome(order) == OrderOutcome.ValidButNotDelivered)
                .toArray(Order[]::new);
    }

    /**
     * @inheritDoc
     */
    @NotNull
    public Order[] getInvalidOrders() {
        // will never be null as empty streams still return an array on toArray
        return Arrays.stream(appData.orders())
                .filter(order -> {
                    OrderOutcome outcome = getOrderOutcome(order);
                    return outcome != OrderOutcome.Delivered
                            && outcome != OrderOutcome.ValidButNotDelivered;
                })
                .toArray(Order[]::new);
    }
}
