package uk.ac.ed.inf;

import uk.ac.ed.inf.Models.Order;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Restaurant;

import java.util.*;

public class DeliveryPlanner {
    // TODO: javadocs for classes, not just methods
    private final FlightpathCalculator flightpathCalculator;
    private final ApplicationData appData;

    public DeliveryPlanner(ApplicationData appData, FlightpathCalculator flightpathCalculator) {
        this.appData = appData;
        this.flightpathCalculator = flightpathCalculator;
    }

    public void setOrderOutcomes() {
        // validate orders
        for (Order order : appData.getOrders()) {
            Restaurant restaurant = Arrays.stream(appData.getRestaurants())
                    .filter(r -> order.getOrderItems().stream()
                            .anyMatch(orderItem -> orderItem.equals(r.getMenu()[0].getName())))
                    .findFirst().orElseThrow(); // TODO: deal with orElseThrow
            order.setRestaurant(restaurant);
            order.setOutcome(order.validateOrder(appData.getRestaurants())); // TODO: revise validate function
        }

        // TODO: revise comment wording
        // calculate flightpath length per order and sort orders in increasing order by required steps
        Arrays.stream(appData.getOrders())
                .filter(order -> order.getOutcome() == OrderOutcome.ValidButNotDelivered)
                .forEach(order -> order.setRequiredSteps(flightpathCalculator.calculateFlightpath(order).size()));
        Arrays.sort(appData.getOrders(), Comparator.comparingInt(Order::getRequiredSteps));

        // get orders up to sum of steps Constants.MAX_MOVES
        int sum = 0;
        for (Order order : appData.getOrders()) {
            int newSum = sum + order.getRequiredSteps();
            if (order.getOutcome() == OrderOutcome.ValidButNotDelivered &&
                    newSum <= Constants.MAX_MOVES) {
                order.setOutcome(OrderOutcome.Delivered);
                sum = newSum;
            }
            if (sum >= Constants.MAX_MOVES) {
                break;
            }
        }

        // TODO: remove print? -> if yes, also change loop to remove newSum
        System.out.println("sum of steps: " + "");
    }

    public Order[] getDeliveredOrders() {
        return Arrays.stream(appData.getOrders())
                .filter(order -> order.getOutcome() == OrderOutcome.Delivered)
                .toArray(Order[]::new);
    }

    public Order[] getValidUndeliveredOrders() {
        return Arrays.stream(appData.getOrders())
                .filter(order -> order.getOutcome() == OrderOutcome.ValidButNotDelivered)
                .toArray(Order[]::new);
    }

    public Order[] getInvalidOrders() {
        return Arrays.stream(appData.getOrders())
                .filter(order -> order.getOutcome() != OrderOutcome.Delivered &&
                                 order.getOutcome() != OrderOutcome.ValidButNotDelivered)
                .toArray(Order[]::new);
    }
}


