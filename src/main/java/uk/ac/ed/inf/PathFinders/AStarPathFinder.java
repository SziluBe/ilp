package uk.ac.ed.inf.PathFinders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Constants;
import uk.ac.ed.inf.Models.Direction;
import uk.ac.ed.inf.Models.Input.Area;
import uk.ac.ed.inf.Models.Input.Restaurant;
import uk.ac.ed.inf.Models.LngLat;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.util.*;

public class AStarPathFinder implements PathFinder {
    private final Map<Restaurant, List<Step>> restaurantsPathsMap = new java.util.HashMap<>();
    private final Map<Restaurant, Boolean> restaurantsPathCalculatedMap = new java.util.HashMap<>();
    private final Area[] noFlyZones;
    private final Area centralArea;
    private final LngLat deliveryOrigin;

    // TODO: mention strategies in report
    public AStarPathFinder(ApplicationData applicationData) {
        this.noFlyZones = applicationData.noFlyZones();
        this.centralArea = applicationData.centralArea();
        this.deliveryOrigin = applicationData.deliveryOrigin();
    }

    // TODO: document that this can return null
    @Nullable
    public List<Step> getFlightPath(Restaurant restaurant) {
        Boolean isPathCalculated = restaurantsPathCalculatedMap.get(restaurant);
        if (isPathCalculated == null || !isPathCalculated) { // this won't throw an error due to short-circuiting
            LngLat target = restaurant.lnglat();
            List<Step> flightPath = aStar(deliveryOrigin, target);
            if (flightPath == null || flightPath.size() == 0) {
                return null;
            }

            var reversedSteps = new ArrayList<Step>();
            for (int i = flightPath.size() - 1; i >= 0; i--) {
                reversedSteps.add(flightPath.get(i).getReverse());
            }

            LngLat firstLngLat = flightPath.get(0).from();
            LngLat lastLngLat = flightPath.get(flightPath.size() - 1).to();

            flightPath.add(new Step(lastLngLat, Constants.HOVER, lastLngLat)); // hover on pickup
            flightPath.addAll(reversedSteps);
            flightPath.add(new Step(firstLngLat, Constants.HOVER, firstLngLat)); // hover on drop-off

            restaurantsPathsMap.put(restaurant, flightPath);
            restaurantsPathCalculatedMap.put(restaurant, true);
            System.out.println("Calculated flightpath for " + restaurant.name() + " length: " + flightPath.size());
        }

        return restaurantsPathsMap.get(restaurant);
    }

    private List<Step> reconstructPath(LngLat current,
                                       Map<LngLat, LngLat> cameFrom,
                                       Map<LngLat, Direction> stepDirs) {
        var totalPath = new ArrayList<Step>();
        while (cameFrom.containsKey(current)) {
            LngLat previous = cameFrom.get(current);
            Direction stepDir = stepDirs.get(current);
            Step step = new Step(
                    previous,
                    stepDir,
                    current
            );
            totalPath.add(0, step);
            current = cameFrom.get(current);
        }
        return totalPath;
    }

    //  based on pseudocode from https://en.wikipedia.org/wiki/A*_search_algorithm
    // TODO: split into smaller methods; e.g. getNeighbours
    private List<Step> aStar(@NotNull LngLat start, @NotNull LngLat goal) {
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