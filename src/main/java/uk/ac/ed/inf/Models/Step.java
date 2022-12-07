package uk.ac.ed.inf.Models;

import org.jetbrains.annotations.NotNull;

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
}
