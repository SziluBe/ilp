package uk.ac.ed.inf.Models;


import java.util.*; // TODO: maybe limit imports

public class Path { // TODO: refactor
    // A* algorithm pseudocode
    //        function reconstruct_path(cameFrom, current)
    //        total_path := {current}
    //        while current in cameFrom.Keys:
    //        current := cameFrom[current]
    //                total_path.prepend(current)
    //                return total_path
    //
    //        // A* finds a path from start to goal.
    //    // h is the heuristic function. h(n) estimates the cost to reach goal from node n.
    //        function A_Star(start, goal, h)
    //        // The set of discovered nodes that may need to be (re-)expanded.
    //        // Initially, only the start node is known.
    //        // This is usually implemented as a min-heap or priority queue rather than a hash-set.
    //        openSet := {start}
    //
    //        // For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from start
    //        // to n currently known.
    //        cameFrom := an empty map
    //
    //        // For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
    //        gScore := map with default value of Infinity
    //        gScore[start] := 0
    //
    //        // For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
    //        // how cheap a path could be from start to finish if it goes through n.
    //        fScore := map with default value of Infinity
    //        fScore[start] := h(start)
    //
    //        while openSet is not empty
    //        // This operation can occur in O(Log(N)) time if openSet is a min-heap or a priority queue
    //        current := the node in openSet having the lowest fScore[] value
    //            if current = goal
    //                return reconstruct_path(cameFrom, current)
    //
    //            openSet.Remove(current)
    //                for each neighbor of current
    //        // d(current,neighbor) is the weight of the edge from current to neighbor
    //        // tentative_gScore is the distance from start to the neighbor through current
    //        tentative_gScore := gScore[current] + d(current, neighbor)
    //                if tentative_gScore < gScore[neighbor]
    //        // This path to neighbor is better than any previous one. Record it!
    //        cameFrom[neighbor] := current
    //        gScore[neighbor] := tentative_gScore
    //        fScore[neighbor] := tentative_gScore + h(neighbor)
    //                    if neighbor not in openSet
    //                        openSet.add(neighbor)
    //
    //                // Open set is empty but goal was never reached
    //                return failure

    static ArrayList<LngLat> reconstructPath(LngLat current, HashMap<LngLat, LngLat> cameFrom) {
        ArrayList<LngLat> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(0, current);
        }
        return totalPath;
    }

    // Set wayPointMode to true to find waypoints
    // Set it to false and pass the current location as start, and the next location as goal
    public static ArrayList<LngLat> findPath(LngLat start, LngLat goal, Area[] noFlyZones, Area centralArea, boolean wayPointMode, boolean weighted) {
        // For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from start
        // to n currently known.
        var cameFrom = new HashMap<LngLat, LngLat>();

        // For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
        var gScore = new HashMap<LngLat, Double>();
        gScore.put(start, 0.0);

        // The set of discovered nodes that may need to be (re-)expanded.
        // Initially, only the start node is known.
        // This is usually implemented as a min-heap or priority queue rather than a hash-set.
        // Remaining distance needs to be weighted higher so we don't end up doing something close to a breadth first search
        PriorityQueue<LngLat> openSet;
        if (weighted) {
             openSet = new PriorityQueue<>(Comparator.comparingDouble(v -> gScore.get(v) + 2 * v.distanceTo(goal)));
        } else {
            openSet = new PriorityQueue<>(Comparator.comparingDouble(v -> gScore.get(v) + v.distanceTo(goal)));
        }
        openSet.add(start);

        // For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
        // how cheap a path could be from start to finish if it goes through n.
        // HashMap<LngLat, Double> fScore = new HashMap<>();
        // fScore.put(start, start.distanceTo(goal));

        // all noFlyZones vertices
        ArrayList<LngLat> allVertices = new ArrayList<>();
        for (Area noFlyZone : noFlyZones) {
            allVertices.addAll(Arrays.asList(noFlyZone.getVertices()));
        }
        allVertices.add(goal);

        var neighbors = new ArrayList<LngLat>();
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
            // This operation can occur in O(Log(N)) time if openSet is a min-heap or a priority queue
            var current = openSet.poll();
            assert current != null; // we don't add null to openSet, nor do we start an iteration with an empty openSet
            if (current.equals(goal) || (!wayPointMode && current.closeTo(goal))) {
                System.out.println("Outer iterations: " + outerIterations);
                System.out.println("Iterations: " + iters);
                System.out.println("Avg duration: " + avgDuration);
                System.out.println("Avg open set size: " + avgOpenSetSize);
                return reconstructPath(current, cameFrom);
            }
            if (current.inArea(centralArea)) {
                beenToCentral = true;
            }

            if (wayPointMode) {
                neighbors = current.verticesVisibleFrom(allVertices, noFlyZones);
            }
            else {
                // TODO: reorder this list based on the slope of the line between current and goal
                // in fact, adjacentLngLats should take a slope parameter, and return the list of vertices
                // sorted by the angle between the line between current and goal, and the line between current and the vertex
                neighbors = current.adjacentLngLats();
            }

            for (LngLat neighbor : neighbors) {
                iters++;
                // d(current,neighbor) is the weight of the edge from current to neighbor
                // tentative_gScore is the distance from start to the neighbor through current
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

                var tentative_gScore = gScore.get(current) + current.distanceTo(neighbor) + centralRevisitPenalty + noFlyZonePenalty;
//                if (greedy) {
//                var tentative_gScore = current.distanceTo(neighbor);
//                }
                if (tentative_gScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    // This path to neighbor is better than any previous one. Record it!
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentative_gScore);
                    // fScore.put(neighbor, tentative_gScore + neighbor.distanceTo(goal));
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
            long endTime = System.nanoTime();
            avgDuration = (long) ((avgDuration * (outerIterations - 1) + (endTime - startTime)) / outerIterations);
            avgOpenSetSize = (int) ((avgOpenSetSize * (outerIterations - 1) + openSet.size()) / outerIterations);
        }

        // Open set is empty but goal was never reached
        System.out.println("Outer iterations: " + outerIterations);
        System.out.println("Iterations: " + iters);
        System.out.println("Avg duration: " + avgDuration);
        System.out.println("Avg open set size: " + avgOpenSetSize);
        return null;
    }
}
