package uk.ac.ed.inf.PathFinder;

import org.jetbrains.annotations.Nullable;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.Restaurant;

import java.util.List;

public interface PathFinder {
    @Nullable
    List<Step> getFlightPath(Restaurant restaurant);
}
