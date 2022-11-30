package uk.ac.ed.inf.PathFinder;

import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.Restaurant;

import java.util.List;

public interface PathFinder {
    List<Step> getFlightPath(Restaurant restaurant);
}
