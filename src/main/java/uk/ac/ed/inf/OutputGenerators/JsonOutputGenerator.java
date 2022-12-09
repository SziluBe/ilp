package uk.ac.ed.inf.OutputGenerators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.DeliveryPlanners.DeliveryPlanner;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Output.OrderOutcome;
import uk.ac.ed.inf.Models.Output.DeliveryEntry;
import uk.ac.ed.inf.Models.Output.FlightPathEntry;
import uk.ac.ed.inf.Models.Step;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an output generator that outputs JSON and GeoJSON files.
 */
public class JsonOutputGenerator implements OutputGenerator {
    private final DeliveryPlanner deliveryPlanner;
    private final ObjectMapper objectMapper;

    /**
     * Default constructor.
     *
     * @param deliveryPlanner The delivery planner.
     * @param objectMapper    The object mapper.
     */
    public JsonOutputGenerator(DeliveryPlanner deliveryPlanner,
                               ObjectMapper objectMapper) {
        this.deliveryPlanner = deliveryPlanner;
        this.objectMapper = objectMapper;
    }

    /**
     * @inheritDoc Each entry in the output JSON array is a JSON object serialised from a {@link DeliveryEntry} object.
     * <p>
     * The output is written to a file called "deliveries-YYYY-MM-DD.json".
     */
    @NotNull
    public String generateDeliveriesOutput(@NotNull Order[] orders, @NotNull String date) {
        var deliveryEntries = new ArrayList<DeliveryEntry>();
        for (var order : orders) {
            // for invalid orders we don't need to worry about calculating the price,
            // for the rest it will be correct anyway
            var orderOutcome = deliveryPlanner.getOrderOutcome(order);
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

        String deliveriesJsonString;

        // write to file
        String filename = "deliveries-" + date + ".json";
        try (FileWriter file = new FileWriter(filename)) {
            deliveriesJsonString = objectMapper.writeValueAsString(deliveryEntries);
            file.write(deliveriesJsonString);
            file.flush();
        } catch (IOException e) {
            System.err.println("An error occurred when generating GeoJSON output: " + filename);
            e.printStackTrace();
        }

        return filename;
    }

    /**
     * @inheritDoc Generates the map output in GeoJSON format.
     * <p>
     * The output is written to a file called "drone-YYYY-MM-DD.json".
     */
    @NotNull
    public String generateFlightPathMapOutput(@NotNull Order[] deliveredOrders, @NotNull String date) {
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

        String flightPathGeoJsonString = flightPathGeoJson.toJson();

        // write to file
        String filename = "drone-" + date + ".geojson";
        try {
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write(flightPathGeoJsonString);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("An error occurred when generating GeoJSON output: " + filename);
            e.printStackTrace();
        }

        return filename;
    }

    /**
     * @inheritDoc Each entry in the output JSON array is a JSON object serialised from a {@link FlightPathEntry} object.
     * <p>
     * The output is written to a file called "flightpath-YYYY-MM-DD.json".
     */
    @NotNull
    public String generateFlightPathOutput(@NotNull Order[] deliveredOrders, @NotNull String date) {
        var outFlightPathEntries = new ArrayList<FlightPathEntry>();
        for (Order order : deliveredOrders) {
            List<Step> stepsForOrder = deliveryPlanner.getPathForOrder(order);
            if (stepsForOrder == null) {
                continue;
            }
            for (var step : stepsForOrder) {
                var dir = step.direction();
                var flightPathEntry = new FlightPathEntry(
                        order.orderNo(),
                        step.from().lng(),
                        step.from().lat(),
                        dir != null ? dir.getAngle() : 0,
                        step.to().lng(),
                        step.to().lat(),
                        System.nanoTime()
                );
                outFlightPathEntries.add(flightPathEntry);
            }
        }
        System.out.println("outFlightPathEntries: " + outFlightPathEntries.size());

        String flightPathJsonString;

        // write to file
        String filename = "flightpath-" + date + ".json";
        try {
            flightPathJsonString = objectMapper.writeValueAsString(outFlightPathEntries);
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write(flightPathJsonString);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("An error occurred when generating JSON output: " + filename);
            e.printStackTrace();
        }

        return filename;
    }
}
