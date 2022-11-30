package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.LngLat;
import uk.ac.ed.inf.Models.OrderOutcome;
import uk.ac.ed.inf.Models.Output.OutFlightPathEntry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        // TODO: command line arguments -> validate them also (including date)
        // TODO: generate output files
        // TODO: documentation
        // TODO: optimise imports further (*)
        // TODO: use var where appropriate
        // TODO: revise access modifiers

        URL baseAddress;
        try {
            baseAddress = new URL("https://ilp-rest.azurewebsites.net/"); // TODO: command line argument
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // TODO: better exception handling (stderr)
        }
        LocalDate date = LocalDate.of(2023, 1, 1); // TODO: get from command line

        String dateString = date.toString();
        LngLat deliveryOrigin = Constants.AT;
        ObjectMapper objectMapper = new ObjectMapper();

        ApplicationData applicationData;
        try {
            applicationData = new ApplicationData(baseAddress, dateString, deliveryOrigin, objectMapper);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: better exception handling (stderr)
        }
        FlightPathCalculator flightpathCalculator = new FlightPathCalculator(applicationData);

        DeliveryPlanner deliveryPlanner = new DeliveryPlanner(applicationData, flightpathCalculator);

        Order[] deliveredOrders = deliveryPlanner.getDeliveredOrders();
        OutPutGenerator outPutGenerator = new OutPutGenerator(deliveryPlanner, flightpathCalculator, objectMapper);

        List<OutFlightPathEntry> flightPathEntries = outPutGenerator.generateOutFlightPath(deliveredOrders);

        for (int j = 0; j < flightPathEntries.size() - 1; j++) {
            assert (flightPathEntries.get(j).distance() >= Constants.MOVE_LENGTH - 0.000001 &&
                    flightPathEntries.get(j).distance() <= Constants.MOVE_LENGTH + 0.000001) ||
                    (flightPathEntries.get(j).distance() >= 0 - 0.000001 &&
                            flightPathEntries.get(j).distance() <= 0 + 0.000001) :
                    "Move not correct length " + flightPathEntries.get(j).distance();
        }

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
        String flightPathGeoJson = outPutGenerator.generateFlightPathGeoJsonOutPut();

        System.out.println("Deliveries: " + deliveries);
        System.out.println("Flightpath Json: " + flightPathJson);
        System.out.println("Flightpath GeoJson: " + flightPathGeoJson);
        System.out.println("Total distance: " + flightPathEntries.size());
        System.out.println("Delivered Orders: " + deliveredOrders.length);
        System.out.println("Valid but undelivered Orders: " + deliveryPlanner.getValidUndeliveredOrders().length);
        System.out.println("Invalid Orders: " + deliveryPlanner.getInvalidOrders().length);

        // TODO: remove this
        assert flightPathEntries.size() == 1922;
        assert applicationData.orders().length == deliveredOrders.length + deliveryPlanner.getValidUndeliveredOrders().length + deliveryPlanner.getInvalidOrders().length :
                "Orders not correctly split into delivered, valid undelivered and invalid";
        assert 29 == deliveredOrders.length : "Delivered orders not correct length " + dateString;
        assert 7 == deliveryPlanner.getInvalidOrders().length : "Delivered orders not correct length " + dateString;
        assert 11 == deliveryPlanner.getValidUndeliveredOrders().length : "Delivered orders not correct length " + dateString;
        // assert that each OrderOutcome is present for the day
        for (OrderOutcome outcome : OrderOutcome.values()) {
            if (outcome == OrderOutcome.Invalid) {
                continue;
            }
            boolean found = false;
            for (Order order : applicationData.orders()) {
                if (deliveryPlanner.getOrderOutcome(order).equals(outcome)) {
                    found = true;
                    break;
                }
            }
            assert found : "Outcome " + outcome + " not found on " + dateString;
        }
    }
}
