package uk.ac.ed.inf.Models;

import org.jetbrains.annotations.NotNull;

public record Step(LngLat from, Direction direction, LngLat to) {
    public double distance() {
        return from.distanceTo(to);
    }

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
