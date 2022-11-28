package uk.ac.ed.inf;

import uk.ac.ed.inf.Models.*;

import java.util.ArrayList;
import java.util.Map;

public class FlightpathCalculator {
    private final Map<Restaurant, LngLat[]> restaurantsPathsMap;
    private final Area[] noFlyZones;
    private final Area centralArea;
    private final LngLat deliveryOrigin;

    public FlightpathCalculator(Restaurant[] restaurants, Area[] noFlyZones, Area centralArea, LngLat deliveryOrigin) {
        this.restaurantsPathsMap = new java.util.HashMap<>();
        for (Restaurant restaurant : restaurants) {
            this.restaurantsPathsMap.put(restaurant, null);
        }
        this.noFlyZones = noFlyZones;
        this.centralArea = centralArea;
        this.deliveryOrigin = deliveryOrigin;
    }

    // TODO: add timer
    public LngLat[] calculateFlightPath(Restaurant restaurant) {
        if (restaurantsPathsMap.get(restaurant) == null) {
            LngLat target = restaurant.getLnglat();
            // TODO: migrate code from findPath to here
            ArrayList<LngLat> waypointPath = Path.findPath(deliveryOrigin, target, noFlyZones, centralArea, true, false);
            ArrayList<LngLat> flightPath = new ArrayList<>();
            // TODO: handle null waypointPath
            assert waypointPath != null;

            LngLat previous = deliveryOrigin;

            for (LngLat waypoint : waypointPath) {
                flightPath.addAll(Path.findPath(previous, waypoint, noFlyZones, centralArea, false, true));
                previous = waypoint;
            }

            restaurantsPathsMap.put(restaurant, flightPath.toArray(new LngLat[0]));
        }

        return restaurantsPathsMap.get(restaurant);
    }

    public LngLat[] calculateFlightPath(Order order) {
        // TODO: deal with null restaurant
        return calculateFlightPath(order.getRestaurant());
    }
}
