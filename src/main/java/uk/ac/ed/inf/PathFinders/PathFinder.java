package uk.ac.ed.inf.PathFinders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Models.Input.Restaurant;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Stores.ApplicationData;

import java.util.List;

/**
 * Represents a pathfinder.
 */
public interface PathFinder {
    /**
     * Gets an instance of the appropriate kind of pathfinder for the given application data.
     *
     * @param appData The application data.
     * @return The appropriate pathfinder.
     */
    @NotNull
    static PathFinder getPathFinder(@NotNull ApplicationData appData) {
        return new AStarPathFinder(appData);
    }

    /**
     * Gets the flight path to the given restaurant.
     *
     * @param restaurant The restaurant.
     * @return The flight path to the restaurant.
     */
    @Nullable
    List<Step> getFlightPath(Restaurant restaurant);
}
