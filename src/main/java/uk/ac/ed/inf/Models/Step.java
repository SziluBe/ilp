package uk.ac.ed.inf.Models;

import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Models.Input.Area;

import java.awt.geom.Line2D;

/**
 * Represents a step in a route.
 */
public record Step(LngLat from, Direction direction, LngLat to) {
    /**
     * Default record constructor.
     *
     * @param LngLat from The starting location of the step.
     *               This should be the same as the to of the previous step.
     * @param Direction direction The direction of the step.
     * @param LngLat to The ending location of the step.
     */

    /**
     * Calculates the distance between the start and end of the step.
     *
     * @return The distance between the start and end of the step.
     */
    public double distance() {
        return from.distanceTo(to);
    }

    /**
     * Gives the reverse of the step.
     *
     * @return A step with the opposite direction, as well as from and to swapped.
     */
    @NotNull
    public Step getReverse() {
        Direction newDirection;
        if (direction == null) {
            newDirection = null;
        } else {
            newDirection = direction.getOpposite();
        }
        return new Step(to, newDirection, from);
    }

    /**
     * Checks if the line segment of the step intersects with any edge of the given area.
     *
     * @param area The area to check.
     * @return Whether the line segment of the step intersects with any edge of the given area.
     */
    public boolean intersectsArea(Area area) {
        LngLat[] vertices = area.vertices();
        for (int j = 0; j < vertices.length - 1; j++) {
            double x1 = vertices[j].lng();
            double y1 = vertices[j].lat();
            double x2 = vertices[j + 1].lng();
            double y2 = vertices[j + 1].lat();
            double x3 = from.lng();
            double y3 = from.lat();
            double x4 = to.lng();
            double y4 = to.lat();
            if (Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
                return true;
            }
        }
        return false;
    }
}
