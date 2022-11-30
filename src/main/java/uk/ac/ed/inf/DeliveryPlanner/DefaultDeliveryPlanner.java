package uk.ac.ed.inf.DeliveryPlanner;

import uk.ac.ed.inf.Stores.ApplicationData;
import uk.ac.ed.inf.Constants;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.MenuItem;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Input.Restaurant;
import uk.ac.ed.inf.PathFinder.PathFinder;

import java.util.*;

public class DefaultDeliveryPlanner implements DeliveryPlanner {
    // TODO: mention in docs that all the "get..." methods can return nulls or collections with nulls present
    // TODO: javadocs for classes, not just methods
    private final PathFinder flightpathFinder;
    private final ApplicationData appData;
    private final Map<Order, Restaurant> orderToRestaurantMap = new HashMap<>();
    private boolean isRestaurantMapCalculated = false;
    private final Map<Order, Integer> orderToRequiredStepsMap = new HashMap<>();
    private boolean isRequiredStepsMapCalculated = false;
    private final Map<Order, OrderOutcome> orderToOutcomeMap = new HashMap<>();
    private boolean isOutcomeMapCalculated = false;

    public DefaultDeliveryPlanner(ApplicationData appData, PathFinder flightpathFinder) {
        this.appData = appData;
        this.flightpathFinder = flightpathFinder;
    }

    private void assignRestaurantsToOrders() {
        // ensure we only run calculations once
        if (isRestaurantMapCalculated) {
            return;
        }

        for (Order order : appData.orders()) {
            for (Restaurant restaurant : appData.restaurants()) {
                if (Arrays.stream(restaurant.menuItems())
                        .anyMatch(menuItem -> menuItem.name().equals(order.orderItems()[0]))) {
                    orderToRestaurantMap.put(order, restaurant);
                    break;
                }
            }
        }
        isRestaurantMapCalculated = true;
    }

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

        for (Order order : appData.orders()) {
            Restaurant restaurant = getRestaurantForOrder(order);
            if (restaurant == null) { // TODO: comment
                continue;
            }
            List<Step> flightPathEntries = flightpathFinder.getFlightPath(restaurant);
            if (flightPathEntries != null) { // TODO: comment
                orderToRequiredStepsMap.put(
                        order,
                        flightPathEntries.size()
                );
            }
        }
        isRequiredStepsMapCalculated = true;
    }

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

        MenuItem[] menuItems = Arrays.stream(appData.restaurants())
                .flatMap(restaurant -> Arrays.stream(restaurant.menuItems()))
                .toArray(MenuItem[]::new);

        // validate orders
        for (Order order : appData.orders()) {
            Restaurant restaurant = getRestaurantForOrder(order);
            orderToOutcomeMap.put(
                    order,
                    order.validateOrder(restaurant, menuItems)
            );
        }

        Order[] deliverableOrders = Arrays.stream(appData.orders())
                .filter(order -> getRequiredStepsForOrder(order) != null)        // TODO: comment
                .sorted(Comparator.comparingInt(this::getRequiredStepsForOrder))
                .toArray(Order[]::new);
        // sort orders by steps
        // calculate final outcomes
        int nSteps = 0;
        for (Order order : deliverableOrders) {
            if (orderToOutcomeMap.get(order) == OrderOutcome.ValidButNotDelivered) {
                Integer addedSteps = getRequiredStepsForOrder(order);
                if (addedSteps != null) {
                    int newSteps = nSteps + addedSteps;
                    if (newSteps <= Constants.MAX_MOVES) {
                        orderToOutcomeMap.put(order, OrderOutcome.Delivered);
                        nSteps = newSteps;
                    }
                }
            }
        }
        isOutcomeMapCalculated = true;
    }

    public List<Step> getPathForOrder(Order order) {
        Restaurant restaurant = getRestaurantForOrder(order);
        if (restaurant == null) {
            return null;
        }

        return flightpathFinder.getFlightPath(restaurant);
    }

    public OrderOutcome getOrderOutcome(Order order) {
        // ensure orders have their outcomes assigned
        assignOutcomesToOrders();
        return orderToOutcomeMap.get(order);
    }

    public Order[] getDeliveredOrders() {
        // will never be null as empty streams still return an array on toArray
        return Arrays.stream(appData.orders())
                .filter(order -> getOrderOutcome(order) == OrderOutcome.Delivered)
                .toArray(Order[]::new);
    }

    public Order[] getValidUndeliveredOrders() {
        // will never be null as empty streams still return an array on toArray
        return Arrays.stream(appData.orders())
                .filter(order -> getOrderOutcome(order) == OrderOutcome.ValidButNotDelivered)
                .toArray(Order[]::new);
    }

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
