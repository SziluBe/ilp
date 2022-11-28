package uk.ac.ed.inf.Models;

import java.net.URL;

public record Area(LngLat[] vertices) {
    public LngLat[] getVertices() {
        return vertices;
    }
}
