package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import uk.ac.ed.inf.DeliveryPlanners.DeliveryPlanner;
import uk.ac.ed.inf.Models.Direction;
import uk.ac.ed.inf.Models.Output.OrderOutcome;
import uk.ac.ed.inf.Models.LngLat;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.OutputGenerators.OutputGenerator;
import uk.ac.ed.inf.PathFinders.PathFinder;
import uk.ac.ed.inf.Stores.ApplicationData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testDeliveryPlanner() throws IOException {
        URL baseAddress = new URL("https://ilp-rest.azurewebsites.net/");
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        ArrayList<LocalDate> dates = new ArrayList<>();
        dates.add(startDate);
        // generate all date strings from 2023-01-01 to 2023-05-31
        int i = 1;
        while (dates.get(dates.size() - 1).isBefore(LocalDate.of(2023, 5, 31))) {
            dates.add(startDate.plusDays(i));
            i++;
        }

        for (LocalDate date : dates) {
            String dateString = date.toString();
            LngLat deliveryOrigin = ApplicationData.AT;
            ObjectMapper objectMapper = new ObjectMapper();

            ApplicationData applicationData = new ApplicationData(baseAddress, dateString, deliveryOrigin, objectMapper);
            PathFinder flightpathFinder = PathFinder.getPathFinder(applicationData);

            DeliveryPlanner deliveryPlanner = DeliveryPlanner.getDeliveryPlanner(applicationData, flightpathFinder);

            Order[] deliveredOrders = deliveryPlanner.getDeliveredOrders();
            OutputGenerator outputGenerator = OutputGenerator.getOutputGenerator(deliveryPlanner);

            List<Step> steps = Arrays.stream(deliveredOrders).map(deliveryPlanner::getPathForOrder).flatMap(
                    orderSteps -> {
                        if (orderSteps == null) {
                            return Stream.empty();
                        } else {
                            return orderSteps.stream();
                        }
                    }).toList();

            for (int j = 0; j < steps.size() - 1; j++) {
                assert (steps.get(j).distance() >= Direction.MOVE_LENGTH - 0.000001 &&
                        steps.get(j).distance() <= Direction.MOVE_LENGTH + 0.000001) ||
                        (steps.get(j).distance() >= 0 - 0.000001 &&
                                steps.get(j).distance() <= 0 + 0.000001) :
                        "Move not correct length " + steps.get(j).distance();
            }

            String deliveries = outputGenerator.generateDeliveriesOutput(applicationData.orders(), applicationData.date());
            String flightPathJson = outputGenerator.generateFlightPathOutput(deliveredOrders, applicationData.date());
            String flightPathGeoJson = outputGenerator.generateFlightPathMapOutput(deliveredOrders, applicationData.date());

            if (dateString.equals("2023-01-01")) {
                System.out.println("Deliveries: " + deliveries);
                System.out.println("Flightpath Json: " + flightPathJson);
                System.out.println("Flightpath GeoJson: " + flightPathGeoJson);
                System.out.println("Total distance: " + steps.size());
                System.out.println("Delivered Orders: " + deliveredOrders.length);
                System.out.println("Valid but undelivered Orders: " + deliveryPlanner.getValidUndeliveredOrders().length);
                System.out.println("Invalid Orders: " + deliveryPlanner.getInvalidOrders().length);
            }

            if (dateString.equals("2023-05-31")) {
                assertEquals(0, deliveryPlanner.getValidUndeliveredOrders().length);
                assertEquals(0, deliveryPlanner.getInvalidOrders().length);
                assertEquals(0, steps.size());
                assertEquals(0, deliveredOrders.length);
            } else {
                assert steps.size() == 1922 : "Total distance not correct " + steps.size();
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
                        OrderOutcome orderOutcome = deliveryPlanner.getOrderOutcome(order);
                        if (orderOutcome == null) {
                            continue;
                        }
                        if (orderOutcome.equals(outcome)) {
                            found = true;
                            break;
                        }
                    }
                    assert found : "Outcome " + outcome + " not found on " + dateString;
                }
            }
        }
    }

    @Test
    public void checkDistanceTo() {
        assertTrue(new LngLat(-3.192473, 55.946233).distanceTo(new LngLat(-3.184319, 55.942617)) > 3.192473 - 3.184319);
    }
}
