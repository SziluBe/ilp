package uk.ac.ed.inf;

import uk.ac.ed.inf.Models.*;
import uk.ac.ed.inf.Models.Input.Area;
import uk.ac.ed.inf.Models.Input.Restaurant;

import java.util.*;

public class FlightPathCalculator {
    private final Map<Restaurant, List<FlightPathEntry>> restaurantsPathsMap = new java.util.HashMap<>();
    private final Map<Restaurant, Boolean> restaurantsPathCalculatedMap = new java.util.HashMap<>();
    private final Area[] noFlyZones;
    private final Area centralArea;
    private final LngLat deliveryOrigin;

    public FlightPathCalculator(ApplicationData applicationData) {
        this.noFlyZones = applicationData.noFlyZones();
        this.centralArea = applicationData.centralArea();
        this.deliveryOrigin = applicationData.deliveryOrigin();
    }

    // TODO: make aStar a strategy
    // TODO: document that this can return null
    public List<FlightPathEntry> getFlightPath(Restaurant restaurant) {
        Boolean isPathCalculated = restaurantsPathCalculatedMap.get(restaurant);
        if (isPathCalculated == null || !isPathCalculated) { // this won't throw an error due to short-circuiting
            LngLat target = restaurant.lnglat();
            List<FlightPathEntry> flightPath = aStar(deliveryOrigin, target);

            restaurantsPathsMap.put(restaurant, flightPath);
            restaurantsPathCalculatedMap.put(restaurant, true);
//            System.out.println("Calculated flightpath for " + restaurant.name() + " length: " + flightPath.size());
        }

        return restaurantsPathsMap.get(restaurant);
    }

    private List<FlightPathEntry> reconstructPath(LngLat current,
                                                          Map<LngLat, LngLat> cameFrom,
                                                          Map<LngLat, Direction> stepDirs) {
        List<FlightPathEntry> totalPath = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            LngLat previous = cameFrom.get(current);
            Direction stepDir = stepDirs.get(current);
            FlightPathEntry flightPathEntry = new FlightPathEntry(
                    previous,
                    stepDir,
                    current
            );
            totalPath.add(0, flightPathEntry);
            current = cameFrom.get(current);
        }
        return totalPath;
    }

    // TODO: credit wikipedia
    // TODO: split into smaller methods; e.g. getNeighbours
    private List<FlightPathEntry> aStar(LngLat start, LngLat goal) {
        // For node n, cameFrom.get(n) is the node immediately preceding it on the cheapest path from start
        // to n currently known.
        var cameFrom = new HashMap<LngLat, LngLat>();
        var stepDirs = new HashMap<LngLat, Direction>();

        // For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
        var gScore = new HashMap<LngLat, Double>();
        gScore.put(start, 0.0);

        // The set of discovered nodes that may need to be (re-)expanded.
        // Initially, only the start node is known.
        // Remaining distance needs to be weighted higher,
        // so we don't end up doing something close to a breadth first search
        PriorityQueue<LngLat> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(v -> gScore.get(v) + 2 * v.distanceTo(goal))
        );
        openSet.add(start);

        double centralRevisitPenalty;
        double noFlyZonePenalty;
        boolean beenToCentral = false;

        int outerIterations = 0;
        int iters = 0;
        long avgDuration = 0;
        int avgOpenSetSize = 0;

        while (!openSet.isEmpty()) { // too many iterations here
            long startTime = System.nanoTime();
            outerIterations++;
            var current = openSet.poll();
            assert current != null; // we don't add null to openSet, nor do we start an iteration with an empty openSet
            if (current.equals(goal) || current.closeTo(goal)) {
                System.out.println("Outer iterations: " + outerIterations);
                System.out.println("Iterations: " + iters);
                System.out.println("Avg duration: " + avgDuration);
                System.out.println("Avg open set size: " + avgOpenSetSize);
                return reconstructPath(current, cameFrom, stepDirs);
            }
            if (current.inArea(centralArea)) {
                beenToCentral = true;
            }

            for (Direction direction : Direction.values()) {
                LngLat neighbor = current.nextPosition(direction);
                iters++;
                centralRevisitPenalty = 0.0;
                noFlyZonePenalty = 0.0;

                if (beenToCentral && !current.inArea(centralArea) && neighbor.inArea(centralArea)) {
                    centralRevisitPenalty = Double.POSITIVE_INFINITY;
                }

                for (Area noFlyZone : noFlyZones) {
                    if (neighbor.inArea(noFlyZone)) {
                        noFlyZonePenalty = Double.POSITIVE_INFINITY;
                    }
                }

                // tentativeGScore is the distance from start to the neighbor through current
                var tentativeGScore = gScore.get(current)
                        + current.distanceTo(neighbor)
                        + centralRevisitPenalty
                        + noFlyZonePenalty;
                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    // This path to neighbor is better than any previous one. Record it!
                    cameFrom.put(neighbor, current);
                    stepDirs.put(neighbor, direction);
                    gScore.put(neighbor, tentativeGScore);
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
            long endTime = System.nanoTime();
            avgDuration = (avgDuration * (outerIterations - 1) + (endTime - startTime)) / outerIterations;
            avgOpenSetSize = (avgOpenSetSize * (outerIterations - 1) + openSet.size()) / outerIterations;
        }

        // Open set is empty but goal was never reached
        System.out.println("Outer iterations: " + outerIterations);
        System.out.println("Iterations: " + iters);
        System.out.println("Avg duration: " + avgDuration);
        System.out.println("Avg open set size: " + avgOpenSetSize);
        return null;
    }
}
