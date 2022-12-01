package uk.ac.ed.inf.PathFinders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Models.Input.Restaurant;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.util.List;

public interface PathFinder {
    @NotNull
    static PathFinder getPathFinder(@NotNull ApplicationData appData) {
        return new AStarPathFinder(appData);
    }

    @Nullable
    List<Step> getFlightPath(Restaurant restaurant);
}
