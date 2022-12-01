package uk.ac.ed.inf.DeliveryPlanner;

import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Stores.ApplicationData;
import uk.ac.ed.inf.PathFinder.PathFinder;

public class DeliveryPlannerFactory {
    @NotNull
    public static DeliveryPlanner getDeliveryPlanner(@NotNull ApplicationData appData, @NotNull PathFinder pathFinder) {
        return new DefaultDeliveryPlanner(appData, pathFinder);
    }
}
