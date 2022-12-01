package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.DeliveryPlanner.DeliveryPlanner;
import uk.ac.ed.inf.DeliveryPlanner.DeliveryPlannerFactory;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.OutPutGenerator.OutPutGenerator;
import uk.ac.ed.inf.OutPutGenerator.OutPutGeneratorFactory;
import uk.ac.ed.inf.PathFinder.PathFinder;
import uk.ac.ed.inf.PathFinder.PathFinderFactory;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        // TODO: generate output files
        // TODO: documentation
        // TODO: optimise imports further (*)
        // TODO: use var where appropriate
        // TODO: revise access modifiers

        if (args.length < 2) {
            System.out.println("Please provide the following arguments: <baseAddress> <date>, optional arguments: <seed>");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        ApplicationData applicationData;
        try {
            applicationData = new ApplicationData(args, objectMapper);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: better exception handling (stderr)
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // TODO: better exception handling (stderr)
        }
        PathFinder flightpathFinder = PathFinderFactory.getPathFinder(applicationData);

        DeliveryPlanner deliveryPlanner = DeliveryPlannerFactory.getDeliveryPlanner(applicationData, flightpathFinder);

        Order[] deliveredOrders = deliveryPlanner.getDeliveredOrders();
        OutPutGenerator outPutGenerator = OutPutGeneratorFactory.getOutPutGenerator(deliveryPlanner);

        // TODO: used only for printing
        List<Step> steps = Arrays.stream(deliveredOrders)
                .map(deliveryPlanner::getPathForOrder)
                .flatMap(orderSteps -> {
                    if (orderSteps == null) {
                        return Stream.of();
                    }
                    return orderSteps.stream();
                })
                .toList();

        String deliveries;
        try {
            deliveries = outPutGenerator.generateDeliveriesOutPut(applicationData.orders());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // TODO: better exception handling (stderr)
        }
        String flightPathJson;
        try {
            flightPathJson = outPutGenerator.generateFlightPathOutPut(deliveredOrders);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // TODO: better exception handling (stderr)
        }
        String flightPathGeoJson;
        try {
            flightPathGeoJson = outPutGenerator.generateFlightPathGeoJsonOutPut();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // TODO: better exception handling (stderr)
        }

        System.out.println("Deliveries Json: " + deliveries);
        System.out.println("Flightpath Json: " + flightPathJson);
        System.out.println("Flightpath GeoJson: " + flightPathGeoJson);
        System.out.println("Total moves: " + steps.size());
        System.out.println("Delivered Orders: " + deliveredOrders.length);
        System.out.println("Valid but undelivered Orders: " + deliveryPlanner.getValidUndeliveredOrders().length);
        System.out.println("Invalid Orders: " + deliveryPlanner.getInvalidOrders().length);
    }
}
