package uk.ac.ed.inf.OutPutGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.DeliveryPlanner.DeliveryPlanner;
import uk.ac.ed.inf.Models.Direction;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Output.DeliveryEntry;
import uk.ac.ed.inf.Models.Output.FlightPathEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultOutPutGenerator implements OutPutGenerator {
    private final DeliveryPlanner deliveryPlanner;
    private final ObjectMapper objectMapper;

    public DefaultOutPutGenerator(DeliveryPlanner deliveryPlanner,
                           ObjectMapper objectMapper) {
        this.deliveryPlanner = deliveryPlanner;
        this.objectMapper = objectMapper;
    }

    public String generateDeliveriesOutPut(Order[] orders) throws JsonProcessingException {
        List<DeliveryEntry> deliveryEntries = new ArrayList<>();
        for (Order order : orders) {
            // for invalid orders we don't need to worry about calculating the price,
            // for the rest it will be correct anyway
            OrderOutcome orderOutcome = deliveryPlanner.getOrderOutcome(order);
            if (orderOutcome == null) {
                orderOutcome = OrderOutcome.Invalid; // not in appData, null restaurant, etc.
            }
            deliveryEntries.add(
                    new DeliveryEntry(
                            order.orderNo(),
                            orderOutcome,
                            order.priceTotalInPence()
                    )
            );
        }

        return objectMapper.writeValueAsString(deliveryEntries);
    }

    public String generateFlightPathGeoJsonOutPut() {
        Order[] deliveredOrders = deliveryPlanner.getDeliveredOrders();
        List<Step> steps = Arrays.stream(deliveredOrders)
                .map(deliveryPlanner::getPathForOrder)
                .flatMap(orderSteps -> {
                    if (orderSteps == null) {
                        return Stream.of();
                    }
                    return orderSteps.stream();
                })
                .toList();

        // generate the points for the geojson from the steps
        List<Point> flightPathPoints = steps.stream()
                .map(step -> Point.fromLngLat(step.to().lng(),
                                              step.to().lat()))
                .collect(Collectors.toList());

        if (flightPathPoints.size() != 0) { // if there are no steps, get(0) will produce an error
            // add the origin to the start of the list
            flightPathPoints.add(0, Point.fromLngLat(steps.get(0).from().lng(),
                    steps.get(0).from().lat()));
        }

        FeatureCollection flightPathGeoJson = FeatureCollection.fromFeatures(
                new Feature[]{Feature.fromGeometry(LineString.fromLngLats(flightPathPoints))}
        );

        return flightPathGeoJson.toJson();
    }

    public String generateFlightPathOutPut(Order[] deliveredOrders) throws JsonProcessingException {
        List<FlightPathEntry> outFlightPathEntries = new ArrayList<>();
        for (Order order : deliveredOrders) {
            List<Step> stepsForOrder = deliveryPlanner.getPathForOrder(order);
            if (stepsForOrder == null) {
                continue;
            }
            for (Step step : stepsForOrder) {
                Direction dir = step.direction();
                FlightPathEntry flightPathEntry = new FlightPathEntry(
                        order.orderNo(),
                        step.from().lng(),
                        step.from().lat(),
                        dir != null ? dir.getAngle() : 0, // TODO: mention in report??
                        step.to().lng(),
                        step.to().lat(),
                        System.nanoTime()
                );
                outFlightPathEntries.add(flightPathEntry);
            }
        }
        System.out.println("outFlightPathEntries = " + outFlightPathEntries.size());
        return objectMapper.writeValueAsString(outFlightPathEntries);
    }
}
