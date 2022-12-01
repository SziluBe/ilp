package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.DeliveryPlanners.DeliveryPlanner;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.OutPutGenerators.OutPutGenerator;
import uk.ac.ed.inf.PathFinders.PathFinder;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        // TODO: documentation
        // TODO: revise access modifiers

        if (args.length < 2) {
            System.err.println("Please provide the following arguments: <date> <baseAddress>, optional arguments: <seed>");
            return;
        }

        var objectMapper = new ObjectMapper();

        ApplicationData applicationData;
        try {
            applicationData = new ApplicationData(args, objectMapper);
        } catch (IOException e) {
            System.err.println("""
                    Failed to read input data from the server; please ensure arguments are correctly formatted:
                    <date>: is in format YYYY-MM-DD\s
                    <baseAddress>: is a valid URL\s
                    , optional arguments: <seed>""");
            e.printStackTrace();
            return;
        } catch (URISyntaxException e) {
            System.err.println("Invalid base address");
            e.printStackTrace();
            return;
        }
        var flightpathFinder = PathFinder.getPathFinder(applicationData);

        var deliveryPlanner = DeliveryPlanner.getDeliveryPlanner(applicationData, flightpathFinder);

        Order[] deliveredOrders = deliveryPlanner.getDeliveredOrders();
        var outPutGenerator = OutPutGenerator.getOutPutGenerator(deliveryPlanner);

        // used only for printing
        List<Step> steps = Arrays.stream(deliveredOrders)
                .map(deliveryPlanner::getPathForOrder)
                .flatMap(orderSteps -> {
                    if (orderSteps == null) {
                        return Stream.of();
                    }
                    return orderSteps.stream();
                })
                .toList();

        String deliveries = outPutGenerator.generateDeliveriesOutPut(applicationData.orders(), applicationData.date());
        String flightPathJson = outPutGenerator.generateFlightPathOutPut(deliveredOrders, applicationData.date());
        String flightPathGeoJson = outPutGenerator.generateFlightPathMapOutPut(deliveredOrders, applicationData.date());

        System.out.println("Deliveries Json: " + deliveries);
        System.out.println("Flightpath Json: " + flightPathJson);
        System.out.println("Flightpath GeoJson: " + flightPathGeoJson);
        System.out.println("Total moves: " + steps.size());
        System.out.println("Delivered Orders: " + deliveredOrders.length);
        System.out.println("Valid but undelivered Orders: " + deliveryPlanner.getValidUndeliveredOrders().length);
        System.out.println("Invalid Orders: " + deliveryPlanner.getInvalidOrders().length);
    }
}
