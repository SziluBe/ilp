package uk.ac.ed.inf.PathFinder;

import uk.ac.ed.inf.Stores.ApplicationData;

public class PathFinderFactory {
    public static PathFinder getPathFinder(ApplicationData appData) {
        return new AStarPathFinder(appData);
    }
}
