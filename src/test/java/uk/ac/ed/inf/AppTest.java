package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mapbox.geojson.*;

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
    public void testGenerateGeojsons() throws IOException {
        Restaurant[] restaurants = Restaurant.getRestaurants(Constants.DEFAULT_BASE_ADDRESS);
        var restaurantLocations = new ArrayList<LngLat>();
        for (Restaurant restaurant : restaurants) {
            restaurantLocations.add(restaurant.getLnglat());
        }

        Area[] noFlyZones = Area.getNoFlyZones(Constants.DEFAULT_BASE_ADDRESS);
        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);

        var waypointPaths = new ArrayList<ArrayList<LngLat>>();
        int counter = 0;
        for (LngLat restaurantLocation : restaurantLocations) {
            counter++;
            if (counter == 5) {
                continue;
            }
            waypointPaths.add(Path.findPath(Constants.AT, restaurantLocation, noFlyZones, centralArea, true, false));
            System.out.println("Path length: " + waypointPaths.get(waypointPaths.size() - 1).size());
        }

        // generate geojson for each waypoint path
        for (int i = 0; i < waypointPaths.size(); i++) {
            var waypointPath = waypointPaths.get(i);
            List<Point> waypointPathPoints = new ArrayList<>();
            for (LngLat waypoint : waypointPath) {
                waypointPathPoints.add(Point.fromLngLat(waypoint.lng(), waypoint.lat()));
            }
            FeatureCollection waypointPathGeojson = FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(LineString.fromLngLats(waypointPathPoints))});
            // print
            String waypointPathGeojsonString = waypointPathGeojson.toJson();
            System.out.println(waypointPathGeojsonString);
        }

        // generate flightpath for each waypoint path
        var flightPaths = new ArrayList<ArrayList<LngLat>>();
//        var flightPaths = waypointPaths;
        counter = 0;
        for (ArrayList<LngLat> waypointPath : waypointPaths) {
            if (counter != 1) { // TODO: A* on steps is too slow >:(
                counter++;
                continue;
            }
            var flightPath = new ArrayList<LngLat>();
            for (int i = 0; i < waypointPath.size() - 1; i++) {
                var p = Path.findPath(waypointPath.get(i), waypointPath.get(i + 1), noFlyZones, centralArea, false, true);
//                flightPath.addAll(p);
            }
//            System.out.println(flightPath.size());
            flightPaths.add(flightPath);
            counter++;
        }

        System.out.println(flightPaths.size());

        // generate geojson for each flightpath
        for (ArrayList<LngLat> flightPath : flightPaths) {
            List<Point> flightPathPoints = new ArrayList<>();
            for (LngLat waypoint : flightPath) {
                flightPathPoints.add(Point.fromLngLat(waypoint.lng(), waypoint.lat()));
            }
            FeatureCollection flightPathGeojson = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(flightPathPoints))});
            // print
            String flightPathGeojsonString = flightPathGeojson.toJson();
            System.out.println(flightPathGeojsonString);
        }

        System.out.println("done");
    }

    @Test
    public void pathTest() throws IOException {
        URL url3;
        try {
            url3 = new URL("https://ilp-rest.azurewebsites.net/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Area[] areas;
        try {
            areas = Area.getNoFlyZones(url3);
//            System.out.println(Arrays.toString(areas));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LngLat AT = new LngLat(-3.186874, 55.944494);
        LngLat restaurant = new LngLat(-3.1912869215011597, 55.945535152517735);

        ArrayList<LngLat> allpoints = new ArrayList<>();
        for (Area area : areas) {
            allpoints.addAll(List.of(area.getVertices()));
        }
        allpoints.add(restaurant);

        for (LngLat point : allpoints) {
//            System.out.println(point.toString());
        }
        List<Point> points2 = new ArrayList<>();
        for (LngLat point : allpoints) {
            points2.add(Point.fromLngLat(point.lng(), point.lat()));
        }
        FeatureCollection fc2 = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(points2))});
        System.out.println("all points");
        System.out.println(fc2.toJson());

        ArrayList<LngLat> visiblePoints = AT.verticesVisibleFrom(allpoints, areas);
        // convert points to geojson and save to file
        List<Point> points3 = new ArrayList<>();
        for (LngLat point : visiblePoints) {
            points3.add(Point.fromLngLat(point.lng(), point.lat()));
        }
        FeatureCollection fc3 = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(points3))});
        System.out.println("visible points");
        System.out.println(fc3.toJson());

        System.out.println("path");
        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
        ArrayList<LngLat> path = Path.findPath(AT,restaurant,areas, centralArea,true, false);
        System.out.println(path.size());
        for (LngLat point : path) {
            System.out.println(point.toString());
        }
        // convert path to geojson and save file
        List<Point> points = new ArrayList<>();
        for (LngLat point : path) {
            System.out.println(path.size());
            points.add(Point.fromLngLat(point.lng(), point.lat()));
        }
        LineString lineString = LineString.fromLngLats(points);
        Feature feature = Feature.fromGeometry(lineString);
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{feature});
        System.out.println(featureCollection.toJson());
    }

    @Test
    public void outsidenotInCentral() throws IOException {
        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
        assertFalse(new LngLat(-4.188, 55.944).inArea(centralArea));
    }

    @Test
    public void cornerNotInCentral() throws IOException {
        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
        assertFalse(new LngLat(-3.192473, 55.946233).inArea(centralArea));
    }

    @Test
    public void edgeNotInCentral() throws IOException {
        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
        assertFalse(new LngLat(-3.188, 55.946233).inArea(centralArea));
    }

    @Test
    public void insideInCentral() throws IOException {
        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
        assertTrue(new LngLat(-3.188, 55.944).inArea(centralArea));
    }

    @Test
    public void checkCentralAreaDeSerialisation() throws IOException {
        Area centralArea = Area.getCentralArea(Constants.DEFAULT_BASE_ADDRESS);
        System.out.println(centralArea);
        assertEquals(4, centralArea.getVertices().length);
    }

    @Test
    public void checkRestaurantDeSerialisation() throws IOException { // also tests Menu serialisation
        assertTrue(Restaurant.getRestaurants(Constants.DEFAULT_BASE_ADDRESS).length > 0);
    }

    @Test
    public void checkOrderDeSerialisation() throws IOException {
        assertTrue(Order.getOrdersFromRestServer(Constants.DEFAULT_BASE_ADDRESS).length > 0);
    }

    @Test
    public void checkOrderDeSerialisationAndDeliveryCost() throws IOException, InvalidPizzaCombination {
        Order[] orders = new ObjectMapper().readValue(new URL(Constants.DEFAULT_BASE_ADDRESS + "orders/"), Order[].class);
        assertEquals(2400, orders[0].getDeliveryCost(Restaurant.getRestaurants(Constants.DEFAULT_BASE_ADDRESS)));
    }

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
//  LngLat[lng=-3.190578818321228, lat=55.94402412577528]
//          LngLat[lng=-3.1899887323379517, lat=55.94284650540911]
//          LngLat[lng=-3.187097311019897, lat=55.94328811724263]
//          LngLat[lng=-3.187682032585144, lat=55.944477740393744]
//          LngLat[lng=-3.190578818321228, lat=55.94402412577528]
//          LngLat[lng=-3.1907182931900024, lat=55.94519570234043]
//          LngLat[lng=-3.1906163692474365, lat=55.94498241796357]
//          LngLat[lng=-3.1900262832641597, lat=55.94507554227258]
//          LngLat[lng=-3.190133571624756, lat=55.94529783810495]
//          LngLat[lng=-3.1907182931900024, lat=55.94519570234043]
//          LngLat[lng=-3.189543485641479, lat=55.94552313663306]
//          LngLat[lng=-3.189382553100586, lat=55.94553214854692]
//          LngLat[lng=-3.189259171485901, lat=55.94544803726933]
//          LngLat[lng=-3.1892001628875732, lat=55.94533688994374]
//          LngLat[lng=-3.189194798469543, lat=55.94519570234043]
//          LngLat[lng=-3.189135789871216, lat=55.94511759833873]
//          LngLat[lng=-3.188138008117676, lat=55.9452738061846]
//          LngLat[lng=-3.1885510683059692, lat=55.946105902745614]
//          LngLat[lng=-3.1895381212234497, lat=55.94555918427592]
//          LngLat[lng=-3.189543485641479, lat=55.94552313663306]
//          LngLat[lng=-3.1876927614212036, lat=55.94520696732767]
//          LngLat[lng=-3.187555968761444, lat=55.9449621408666]
//          LngLat[lng=-3.186981976032257, lat=55.94505676722831]
//          LngLat[lng=-3.1872327625751495, lat=55.94536993377657]
//          LngLat[lng=-3.1874459981918335, lat=55.9453361389472]
//          LngLat[lng=-3.1873735785484314, lat=55.94519344934259]
//          LngLat[lng=-3.1875935196876526, lat=55.94515665035927]
//          LngLat[lng=-3.187624365091324, lat=55.94521973430925]
//          LngLat[lng=-3.1876927614212036, lat=55.94520696732767]
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
