package uk.ac.ed.inf.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Area(LngLat[] vertices) {
//    private final LngLat[] vertices;

    @JsonCreator
    private Area(@JsonProperty("coordinates") Double[][] verticesCoords) {
        this(Area.fromDoubleArray(verticesCoords));
    }

    private static LngLat[] fromDoubleArray(Double[][] verticesCoords) {
        LngLat[] vertices = new LngLat[verticesCoords.length];
        for (int i = 0; i < verticesCoords.length; i++) {
            vertices[i] = new LngLat(verticesCoords[i][0], verticesCoords[i][1]);
        }
        return vertices;
    }

    public Area(LngLat[] vertices) {
        this.vertices = vertices;
    }

    public LngLat[] getVertices() {
        return vertices;
    }
}
