package uk.ac.ed.inf;

import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Input.Restaurant;

import java.util.*;

public class DeliveryPlanner {
    // TODO: javadocs for classes, not just methods
    private final FlightPathCalculator flightpathCalculator;
    private final ApplicationData appData;
    private final Map<Order, Restaurant> orderToRestaurantMap;
    private final Map<Order, Integer> orderToRequiredStepsMap;
    private final Map<Order, OrderOutcome> orderToOutcomeMap;

    public DeliveryPlanner(ApplicationData appData, FlightPathCalculator flightpathCalculator) {
        this.appData = appData;
        this.flightpathCalculator = flightpathCalculator;
        this.orderToRestaurantMap = new HashMap<>();
        this.orderToRequiredStepsMap = new HashMap<>();
        this.orderToOutcomeMap = new HashMap<>();
    }

    private void assignRestaurantsToOrders() {
        // ensure we only run calculations once
        if (orderToRestaurantMap.size() == appData.orders().length) {
            return;
        }

        for (Order order : appData.orders()) {
            for (Restaurant restaurant : appData.restaurants()) {
                if (order.orderItems().stream()
                        .anyMatch(orderItem -> orderItem.equals(restaurant.menuItems()[0].name()))) {
                    orderToRestaurantMap.put(order, restaurant);
                    break;
                }
            }
        }
    }

    public Restaurant getRestaurantForOrder(Order order) {
        // ensure all orders have a restaurant assigned
        assignRestaurantsToOrders();
        return orderToRestaurantMap.get(order);
    }

    private void assignRequiredStepsToOrders() {
        // ensure we only run calculations once
        if (orderToRequiredStepsMap.size() == appData.orders().length) {
            return;
        }
        // ensure all orders have a restaurant assigned
        assignRestaurantsToOrders();

        for (Order order : appData.orders()) {
            orderToRequiredStepsMap.put(
                    order,
                    2 * flightpathCalculator.calculateFlightpath(orderToRestaurantMap.get(order)).size() + 2
            );
        }
    }

    public int getRequiredStepsForOrder(Order order) {
        // ensure all orders have a restaurant assigned
        assignRequiredStepsToOrders();

        return orderToRequiredStepsMap.get(order);
    }

    private void assignOutcomesToOrders() {
        // ensure we only run calculations once
        if (orderToOutcomeMap.size() == appData.orders().length) {
            return;
        }
        // ensure all orders have their required steps assigned
        assignRequiredStepsToOrders();

        for (Order order : appData.orders()) {
            orderToOutcomeMap.put(
                    order,
                    order.validateOrder(getRestaurantForOrder(order), appData.restaurants()) // TODO: revise validate function
            );
        }

        // sort orders by steps
        Arrays.sort(appData.orders(), Comparator.comparingInt(orderToRequiredStepsMap::get));
        // calculate outcomes
        int steps = 0;
        for (Order order : appData.orders()) {
            if (getOrderOutcome(order) == OrderOutcome.ValidButNotDelivered) {
                int newSteps = steps + getRequiredStepsForOrder(order);
                if (newSteps > Constants.MAX_MOVES) {
                    orderToOutcomeMap.put(order, OrderOutcome.ValidButNotDelivered);
                } else {
                    orderToOutcomeMap.put(order, OrderOutcome.Delivered);
                    steps = newSteps;
                }
            }
        }
    }

    public OrderOutcome getOrderOutcome(Order order) {
        // ensure orders have their outcomes assigned
        assignOutcomesToOrders();

        return orderToOutcomeMap.get(order);
    }

    public Order[] getDeliveredOrders() {
        return Arrays.stream(appData.orders())
                .filter(order -> getOrderOutcome(order) == OrderOutcome.Delivered)
                .toArray(Order[]::new);
    }

    public Order[] getValidUndeliveredOrders() {
        return Arrays.stream(appData.orders())
                .filter(order -> getOrderOutcome(order) == OrderOutcome.ValidButNotDelivered)
                .toArray(Order[]::new);
    }

    public Order[] getInvalidOrders() {
        return Arrays.stream(appData.orders())
                .filter(order -> {
                    OrderOutcome outcome = getOrderOutcome(order);
                    return outcome != OrderOutcome.Delivered
                            && outcome != OrderOutcome.ValidButNotDelivered;
                })
                .toArray(Order[]::new);
    }
}
