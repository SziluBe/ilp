package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.Models.*;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Output.OutFlightPathEntry;

import static org.junit.Assert.*;

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
            LngLat deliveryOrigin = Constants.AT;
            ObjectMapper objectMapper = new ObjectMapper();

            ApplicationData applicationData = new ApplicationData(baseAddress, dateString, deliveryOrigin, objectMapper);
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

            String deliveries = outPutGenerator.generateDeliveriesOutPut(applicationData.orders());
            String flightPathJson = outPutGenerator.generateFlightPathOutPut(deliveredOrders);
            String flightPathGeoJson = outPutGenerator.generateFlightPathGeoJsonOutPut();

            if (dateString.equals("2023-01-01")) {
                System.out.println("Deliveries: " + deliveries);
                System.out.println("Flightpath Json: " + flightPathJson);
                System.out.println("Flightpath GeoJson: " + flightPathGeoJson);
                System.out.println("Total distance: " + flightPathEntries.size());
                System.out.println("Delivered Orders: " + deliveredOrders.length);
                System.out.println("Valid but undelivered Orders: " + deliveryPlanner.getValidUndeliveredOrders().length);
                System.out.println("Invalid Orders: " + deliveryPlanner.getInvalidOrders().length);
            }

            if (dateString.equals("2023-05-31")) {
                assertEquals(0, deliveryPlanner.getValidUndeliveredOrders().length);
                assertEquals(0, deliveryPlanner.getInvalidOrders().length);
                assertEquals(0, flightPathEntries.size());
                assertEquals(0, deliveredOrders.length);
            }
            else {
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
    }

//    @Test
//    public void testGenerateGeojsons() throws IOException {
//        Restaurant[] restaurants = Restaurant.getRestaurants(Constants.DEFAULT_BASE_ADDRESS);
//        var restaurantLocations = new ArrayList<LngLat>();
//        for (Restaurant restaurant : restaurants) {
//            restaurantLocations.add(restaurant.getLnglat());
//        }
//
//        Area[] noFlyZones = Area.getNoFlyZones(Constants.DEFAULT_BASE_ADDRESS);
//        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
//
//        var waypointPaths = new ArrayList<ArrayList<LngLat>>();
//        int counter = 0;
//        for (LngLat restaurantLocation : restaurantLocations) {
//            counter++;
//            if (counter == 5) {
//                continue;
//            }
//            waypointPaths.add(Path.findPath(Constants.AT, restaurantLocation, noFlyZones, centralArea, true, false));
//            System.out.println("Path length: " + waypointPaths.get(waypointPaths.size() - 1).size());
//        }
//
//        // generate geojson for each waypoint path
//        for (int i = 0; i < waypointPaths.size(); i++) {
//            var waypointPath = waypointPaths.get(i);
//            List<Point> waypointPathPoints = new ArrayList<>();
//            for (LngLat waypoint : waypointPath) {
//                waypointPathPoints.add(Point.fromLngLat(waypoint.getLng(), waypoint.getLat()));
//            }
//            FeatureCollection waypointPathGeojson = FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(LineString.fromLngLats(waypointPathPoints))});
//            // print
//            String waypointPathGeojsonString = waypointPathGeojson.toJson();
//            System.out.println(waypointPathGeojsonString);
//        }
//
//        // generate flightpath for each waypoint path
//        var flightPaths = new ArrayList<ArrayList<LngLat>>();
////        var flightPaths = waypointPaths;
//        counter = 0;
//        for (ArrayList<LngLat> waypointPath : waypointPaths) {
//            if (counter == 5) {
//                counter++;
//                continue;
//            }
//            var flightPath = new ArrayList<LngLat>();
//            for (int i = 0; i < waypointPath.size() - 1; i++) {
//                var p = Path.findPath(waypointPath.get(i), waypointPath.get(i + 1), noFlyZones, centralArea, false, true);
//                flightPath.addAll(p);
//            }
//            flightPaths.add(flightPath);
//            System.out.println("Path length: " + flightPaths.get(flightPaths.size() - 1).size());
//            counter++;
//        }
//
//        System.out.println(flightPaths.size());
//
//        // generate geojson for each flightpath
//        for (ArrayList<LngLat> flightPath : flightPaths) {
//            List<Point> flightPathPoints = new ArrayList<>();
//            for (LngLat waypoint : flightPath) {
//                flightPathPoints.add(Point.fromLngLat(waypoint.getLng(), waypoint.getLat()));
//            }
//            FeatureCollection flightPathGeojson = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(flightPathPoints))});
//            // print
//            String flightPathGeojsonString = flightPathGeojson.toJson();
//            System.out.println(flightPathGeojsonString);
//        }
//
//        System.out.println("done");
//    }

//    @Test
//    public void pathTest() throws IOException {
//        URL url3;
//        try {
//            url3 = new URL("https://ilp-rest.azurewebsites.net/");
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//        Area[] areas;
//        try {
//            areas = Area.getNoFlyZones(url3);
////            System.out.println(Arrays.toString(areas));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        LngLat AT = new LngLat(-3.186874, 55.944494);
//        LngLat restaurant = new LngLat(-3.1912869215011597, 55.945535152517735);
//
//        ArrayList<LngLat> allpoints = new ArrayList<>();
//        for (Area area : areas) {
//            allpoints.addAll(List.of(area.getVertices()));
//        }
//        allpoints.add(restaurant);
//
//        for (LngLat point : allpoints) {
////            System.out.println(point.toString());
//        }
//        List<Point> points2 = new ArrayList<>();
//        for (LngLat point : allpoints) {
//            points2.add(Point.fromLngLat(point.getLng(), point.getLat()));
//        }
//        FeatureCollection fc2 = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(points2))});
//        System.out.println("all points");
//        System.out.println(fc2.toJson());
//
//        ArrayList<LngLat> visiblePoints = AT.verticesVisibleFrom(allpoints, areas);
//        // convert points to geojson and save to file
//        List<Point> points3 = new ArrayList<>();
//        for (LngLat point : visiblePoints) {
//            points3.add(Point.fromLngLat(point.getLng(), point.getLat()));
//        }
//        FeatureCollection fc3 = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(points3))});
//        System.out.println("visible points");
//        System.out.println(fc3.toJson());
//
//        System.out.println("path");
//        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
//        ArrayList<LngLat> path = Path.findPath(AT,restaurant,areas, centralArea,true, false);
//        System.out.println(path.size());
//        for (LngLat point : path) {
//            System.out.println(point.toString());
//        }
//        // convert path to geojson and save file
//        List<Point> points = new ArrayList<>();
//        for (LngLat point : path) {
//            System.out.println(path.size());
//            points.add(Point.fromLngLat(point.getLng(), point.getLat()));
//        }
//        LineString lineString = LineString.fromLngLats(points);
//        Feature feature = Feature.fromGeometry(lineString);
//        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{feature});
//        System.out.println(featureCollection.toJson());
//    }

//    @Test
//    public void outsidenotInCentral() throws IOException {
//        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
//        assertFalse(new LngLat(-4.188, 55.944).inArea(centralArea));
//    }

//    @Test
//    public void cornerNotInCentral() throws IOException {
//        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
//        assertFalse(new LngLat(-3.192473, 55.946233).inArea(centralArea));
//    }

//    @Test
//    public void edgeNotInCentral() throws IOException {
//        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
//        assertFalse(new LngLat(-3.188, 55.946233).inArea(centralArea));
//    }

//    @Test
//    public void insideInCentral() throws IOException {
//        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
//        assertTrue(new LngLat(-3.188, 55.944).inArea(centralArea));
//    }

//    @Test
//    public void checkCentralAreaDeSerialisation() throws IOException {
//        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
//        System.out.println(centralArea);
//        assertEquals(4, centralArea.getVertices().length);
//    }

//    @Test
//    public void checkRestaurantDeSerialisation() throws IOException { // also tests Menu serialisation
//        assertTrue(Restaurant.getRestaurants(Constants.DEFAULT_BASE_ADDRESS).length > 0);
//    }

//    @Test
//    public void checkOrderDeSerialisation() throws IOException {
//        assertTrue(Order.getOrdersFromRestServer(Constants.DEFAULT_BASE_ADDRESS, "2023-01-01").length > 0);
//    }

//    @Test
//    public void checkOrderDeSerialisationAndDeliveryCost() throws IOException, InvalidPizzaCombination {
//        Order[] orders = new ObjectMapper().readValue(new URL(Constants.DEFAULT_BASE_ADDRESS + "orders/"), Order[].class);
//        assertEquals(2400, orders[0].getDeliveryCost(Restaurant.getRestaurants(Constants.DEFAULT_BASE_ADDRESS)));
//    }

    @Test
    public void checkDistanceTo() {
        assertTrue(new LngLat(-3.192473, 55.946233).distanceTo(new LngLat(-3.184319, 55.942617)) > 3.192473 - 3.184319);
    }

//    @Test // removed static method after testing
//    public void checkCardValid() {
//        // visa, mastercard, amex
//        String[] validCards = {"4111111111111111", "5555555555554444", "371449635398431"};
//        for (String card : validCards) {
//            assertTrue(Order.cardValid(card));
//        }
//    }

//    @Test // we want the pizza combination method private, so we exclude this test after running it once
//    public void checkOrderSerialisationAndCombination() throws IOException, InvalidPizzaCombination {
//        Order[] orders = new ObjectMapper().readValue(new URL(Constants.DEFAULT_BASE_ADDRESS + "orders/"), Order[].class);
//        assertTrue(orders[0].checkPizzaCombination(Restaurant.getRestaurantsFromRestServer(Constants.DEFAULT_BASE_ADDRESS), orders[0].getOrderItems()));
//    }
}



 // convert to geojson
//  LngLat[getLng=-3.190578818321228, getLat=55.94402412577528]
//          LngLat[getLng=-3.1899887323379517, getLat=55.94284650540911]
//          LngLat[getLng=-3.187097311019897, getLat=55.94328811724263]
//          LngLat[getLng=-3.187682032585144, getLat=55.944477740393744]
//          LngLat[getLng=-3.190578818321228, getLat=55.94402412577528]
//          LngLat[getLng=-3.1907182931900024, getLat=55.94519570234043]
//          LngLat[getLng=-3.1906163692474365, getLat=55.94498241796357]
//          LngLat[getLng=-3.1900262832641597, getLat=55.94507554227258]
//          LngLat[getLng=-3.190133571624756, getLat=55.94529783810495]
//          LngLat[getLng=-3.1907182931900024, getLat=55.94519570234043]
//          LngLat[getLng=-3.189543485641479, getLat=55.94552313663306]
//          LngLat[getLng=-3.189382553100586, getLat=55.94553214854692]
//          LngLat[getLng=-3.189259171485901, getLat=55.94544803726933]
//          LngLat[getLng=-3.1892001628875732, getLat=55.94533688994374]
//          LngLat[getLng=-3.189194798469543, getLat=55.94519570234043]
//          LngLat[getLng=-3.189135789871216, getLat=55.94511759833873]
//          LngLat[getLng=-3.188138008117676, getLat=55.9452738061846]
//          LngLat[getLng=-3.1885510683059692, getLat=55.946105902745614]
//          LngLat[getLng=-3.1895381212234497, getLat=55.94555918427592]
//          LngLat[getLng=-3.189543485641479, getLat=55.94552313663306]
//          LngLat[getLng=-3.1876927614212036, getLat=55.94520696732767]
//          LngLat[getLng=-3.187555968761444, getLat=55.9449621408666]
//          LngLat[getLng=-3.186981976032257, getLat=55.94505676722831]
//          LngLat[getLng=-3.1872327625751495, getLat=55.94536993377657]
//          LngLat[getLng=-3.1874459981918335, getLat=55.9453361389472]
//          LngLat[getLng=-3.1873735785484314, getLat=55.94519344934259]
//          LngLat[getLng=-3.1875935196876526, getLat=55.94515665035927]
//          LngLat[getLng=-3.187624365091324, getLat=55.94521973430925]
//          LngLat[getLng=-3.1876927614212036, getLat=55.94520696732767]
//          0

// "geometry": {
//            "type": "LineString",
//            "coordinates": [
//              [
//                -3.1899887323379517,
//                55.94284650540911
//              ],
//              [
//                -3.187097311019897,
//                55.94328811724263
//              ],
//              [
//                -3.187682032585144,
//                55.944477740393744
//              ],
//              [
//                -3.190578818321228,
//                55.94402412577528
//              ],
//              [
//                -3.1907182931900024,
//                55.94519570234043
//              ],
//              [
//                -3.1906163692474365,
//                55.94498241796357
//              ],
//              [
//                -3.1900262832641597,
//                55.94507554227258
//              ],
//              [
//                -3.190133571624756,
//                55.94529783810495
//              ],
//              [
//                -3.1907182931900024,
//                55.94519570234043
//              ],
//              [
//                -3.189543485641479,
//                55.94552313663306
//              ],
//              [
//                -3.189382553100586,
//                55.94553214854692
//              ],
//              [
//                -3.189259171485901,
//                55.94544803726933
//              ],
//              [
//                -3.1892001628875732,
//                55.94533688994374
//              ],
//              [
//                -3.189194798469543,
//                55.94519570234043
//              ],
//              [
//                -3.189135789871216,
//                55.94511759833873
//              ],
//              [
//                -3.188138008117676,
//                55.9452738061846
//              ],
//              [
//                -3.1885510683059692,
//                55.946105902745614
//              ],
//              [
//                -3.1895381212234497,
//                55.94555918427592
//              ],
//              [
//                -3.189543485641479,
//                55.94552313663306
//              ],
//              [
//                -3.1876927614212036,
//                55.94520696732767
//              ],
//              [
//                -3.187555968761444,
//                55.9449621408666
//              ],
//              [
//                -3.186981976032257,
//                55.94505676722831
//              ],
//              [
//                -3.1872327625751495,
//                55.94536993377657
//              ],
//              [
//                -3.1874459981918335,
//                55.9453361389472
//              ],
//              [
//                -3.1873735785484314,
//                55.94519344934259
//              ],
//              [
//                -3.1875935196876526,
//                55.94515665035927
//              ],
//              [
//                -3.187624365091324,
//                55.94521973430925
//              ],
//              [
//                -3.1876927614212036,
//                55.94520696732767
//              ]
//            ]
//          },
