package uk.ac.ed.inf;

import java.util.*;

public class Path {
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

    static ArrayList<LngLat> findPath(LngLat start, LngLat goal, Area[] noFlyZones) {
        // For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from start
        // to n currently known.
        HashMap<LngLat, LngLat> cameFrom = new HashMap<>();

        // For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
        HashMap<LngLat, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);

        // The set of discovered nodes that may need to be (re-)expanded.
        // Initially, only the start node is known.
        // This is usually implemented as a min-heap or priority queue rather than a hash-set.
        PriorityQueue<LngLat> openSet = new PriorityQueue<>(Comparator.comparingDouble(v -> gScore.get(v) + v.distanceTo(goal)));
        openSet.add(start);

        // For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
        // how cheap a path could be from start to finish if it goes through n.
        HashMap<LngLat, Double> fScore = new HashMap<>();
        fScore.put(start, start.distanceTo(goal));

        // all noFlyZones vertices
        ArrayList<LngLat> allVertices = new ArrayList<>();
        for (Area noFlyZone : noFlyZones) {
            allVertices.addAll(Arrays.asList(noFlyZone.getVertices()));
        }
        allVertices.add(goal);

        while (!openSet.isEmpty()) {
            // This operation can occur in O(Log(N)) time if openSet is a min-heap or a priority queue
            LngLat current = openSet.poll();
            if (current.equals(goal)) {
                return reconstructPath(current, cameFrom);
            }

            openSet.remove(current);
            for (LngLat neighbor : current.verticesVisibleFrom(allVertices, noFlyZones)) {
                // d(current,neighbor) is the weight of the edge from current to neighbor
                // tentative_gScore is the distance from start to the neighbor through current
                double tentative_gScore = gScore.get(current) + current.distanceTo(neighbor);
                if (tentative_gScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    // This path to neighbor is better than any previous one. Record it!
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentative_gScore);
                    fScore.put(neighbor, tentative_gScore + neighbor.distanceTo(goal));
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // Open set is empty but goal was never reached
        return null;
    }
}
