package uk.ac.ed.inf.DeliveryPlanner;

import uk.ac.ed.inf.Stores.ApplicationData;
import uk.ac.ed.inf.PathFinder.PathFinder;

public class DeliveryPlannerFactory {
    public static DeliveryPlanner getDeliveryPlanner(ApplicationData appData, PathFinder pathFinder) {
        return new DefaultDeliveryPlanner(appData, pathFinder);
    }
}
