package uk.ac.ed.inf.Models;

public record Step(LngLat from, Direction direction, LngLat to) {
    public double distance() {
        return from.distanceTo(to);
    }

    public Step getReverse() {
        return new Step(to, direction.getOpposite(), from);
    }
}
