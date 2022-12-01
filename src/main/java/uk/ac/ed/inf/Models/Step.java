package uk.ac.ed.inf.Models;

import org.jetbrains.annotations.NotNull;

public record Step(LngLat from, Direction direction, LngLat to) {
    public double distance() {
        return from.distanceTo(to);
    }

    @NotNull
    public Step getReverse() {
        return new Step(to, direction.getOpposite(), from);
    }
}
