package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.Models.LngLat;
import uk.ac.ed.inf.Models.Order;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Restaurant;
import uk.ac.ed.inf.Serializer.Deliveries;
import uk.ac.ed.inf.Serializer.DeliveryEntry;
import uk.ac.ed.inf.Serializer.OutFlightPath;

import java.util.*;

public class DeliveryPlanner {
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

        // calculate flightpath length per order and sort orders in increasing order by required steps
        for (Order order : appData.getOrders()) {
            int steps = flightpathCalculator.calculateFlightPath(order).length + 2; // +2 for collection and delivery
            order.setRequiredSteps(steps);
        }
        Arrays.sort(appData.getOrders(), Comparator.comparingInt(Order::getRequiredSteps));

        // get orders up to sum of steps Constants.MAX_MOVES
        int sum = 0;
        int i = 0;
        while (sum <= Constants.MAX_MOVES && i < appData.getOrders().length) {
            if (appData.getOrders()[i].getOutcome() == OrderOutcome.ValidButNotDelivered) {
                sum += appData.getOrders()[i].getRequiredSteps();
                appData.getOrders()[i].setOutcome(OrderOutcome.Delivered);
            }
            i++;
        }
    }

    private LngLat[] generateFullFlightpath(Order[] orders) {
        ArrayList<LngLat> flightPath = new ArrayList<>();
        for (Order order : orders) {
            // memoised flightpath so calling calculateFlightPath doesn't recalculate
            LngLat[] orderFlightPath = flightpathCalculator.calculateFlightPath(order);
            flightPath.addAll(Arrays.asList(orderFlightPath)); // to restaurant

            flightPath.add(order.getRestaurant().getLnglat()); // hover

            ArrayList<LngLat> reversePath = new ArrayList<>(Arrays.asList(orderFlightPath));
            Collections.reverse(reversePath);
            flightPath.addAll(reversePath); // from restaurant

            flightPath.add(appData.getDeliveryOrigin()); // hover
        }
        return flightPath.toArray(new LngLat[0]);
    }

    public OutFlightPath generateFlightPathOutPut(Order[] orders) {
        LngLat[] flightPath = generateFullFlightpath(orders);
        // TODO + how do we get angles?
        return null;
    }

    public Deliveries generateDeliveriesOutPut(Order[] orders) {
        ArrayList<DeliveryEntry> deliveryEntries = new ArrayList<>();
        for (Order order : orders) {
            // for invalid orders we don't need to worry about calculating the price,
            // for the rest it will be correct anyway
            deliveryEntries.add(new DeliveryEntry(order.getOrderNo(), order.getOutcome(), order.getPriceTotalInPence()));
        }
        return new Deliveries(deliveryEntries.toArray(new DeliveryEntry[0]));
    }

    public FeatureCollection generateFlightPathGeoJson() {
        LngLat[] flightPath = generateFullFlightpath(appData.getOrders());
        List<Point> flightPathPoints = new ArrayList<>();
        for (LngLat waypoint : flightPath) {
            flightPathPoints.add(Point.fromLngLat(waypoint.lng(), waypoint.lat()));
        }
        FeatureCollection flightPathGeojson = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(flightPathPoints))});
        // print
        String flightPathGeojsonString = flightPathGeojson.toJson();
        System.out.println(flightPathGeojsonString);

        return flightPathGeojson;
    }
}


