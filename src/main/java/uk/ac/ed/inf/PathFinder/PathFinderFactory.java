package uk.ac.ed.inf.PathFinder;

import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Stores.ApplicationData;

public class PathFinderFactory {
    @NotNull
    public static PathFinder getPathFinder(@NotNull ApplicationData appData) {
        return new AStarPathFinder(appData);
    }
}
