package uk.ac.ed.inf;

import uk.ac.ed.inf.Models.*;

// TODO: optimise imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class FlightpathCalculator {
    private final Map<Restaurant, ArrayList<LngLat>> restaurantsPathsMap;
    private final Area[] noFlyZones;
    private final Area centralArea;
    private final LngLat deliveryOrigin;

    // TODO: remove?
//    public FlightpathCalculator(Restaurant[] restaurants, Area[] noFlyZones, Area centralArea, LngLat deliveryOrigin) {
//        this.restaurantsPathsMap = new java.util.HashMap<>();
//        for (Restaurant restaurant : restaurants) {
//            this.restaurantsPathsMap.put(restaurant, null);
//        }
//        this.noFlyZones = noFlyZones;
//        this.centralArea = centralArea;
//        this.deliveryOrigin = deliveryOrigin;
//    }

    public FlightpathCalculator(ApplicationData applicationData) {
        this.restaurantsPathsMap = new java.util.HashMap<>();
        for (Restaurant restaurant : applicationData.getRestaurants()) {
            this.restaurantsPathsMap.put(restaurant, null);
        }
        this.noFlyZones = applicationData.getNoFlyZones();
        this.centralArea = applicationData.getCentralArea();
        this.deliveryOrigin = applicationData.getDeliveryOrigin();
    }

    // TODO: add timer
    public ArrayList<LngLat> calculateFlightpath(Restaurant restaurant) {
        if (restaurantsPathsMap.get(restaurant) == null) {
            LngLat target = restaurant.getLnglat();
            // TODO: migrate code from findPath to here?
            ArrayList<LngLat> flightPath = Path.findPath(deliveryOrigin, target, noFlyZones, centralArea, false, true);
            // TODO/WARNING: wayPointMode actually gets 1 less delivery done on 2023-01-01, so don't use it!
//            ArrayList<LngLat> flightPath = new ArrayList<>();
//            // TODO: handle null waypointPath
//            assert waypointPath != null;
//
//            LngLat previous = deliveryOrigin;
//
//            for (LngLat waypoint : waypointPath) {
//                flightPath.addAll(Path.findPath(previous, waypoint, noFlyZones, centralArea, false, true));
//                previous = waypoint;
//            }

            restaurantsPathsMap.put(restaurant, flightPath);
        }

        return restaurantsPathsMap.get(restaurant);
    }

    public ArrayList<LngLat> calculateFlightpath(Order order) {
        ArrayList<LngLat> flightPath = new ArrayList<>();

        // flightpath is memoised per restaurant to save runtime
        // TODO: deal with null restaurant
        ArrayList<LngLat> orderFlightPath = calculateFlightpath(order.getRestaurant());
        flightPath.addAll(orderFlightPath); // to restaurant

        flightPath.add(orderFlightPath.get(orderFlightPath.size() - 1)); // hover

        ArrayList<LngLat> reversePath = new ArrayList<>(orderFlightPath);
        Collections.reverse(reversePath);
        flightPath.addAll(reversePath); // from restaurant

        flightPath.add(orderFlightPath.get(0)); // hover

        return flightPath;
    }

    public ArrayList<LngLat> calculateFlightpath(Order[] orders) {
        ArrayList<LngLat> flightPath = new ArrayList<>();

        for (Order order : orders) {
            flightPath.addAll(calculateFlightpath(order));
        }

        return flightPath;
    }
}
