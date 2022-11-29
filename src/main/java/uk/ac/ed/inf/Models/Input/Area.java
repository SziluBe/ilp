package uk.ac.ed.inf.Models.Input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.Models.LngLat;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Area(LngLat[] vertices) {
    @JsonCreator
    private Area(@JsonProperty("coordinates") Double[][] verticesCoords) {
        this(Area.from2dDoubleArray(verticesCoords));
    }

    private static LngLat[] from2dDoubleArray(Double[][] verticesCoords) {
        LngLat[] vertices = new LngLat[verticesCoords.length];
        for (int i = 0; i < verticesCoords.length; i++) {
            vertices[i] = new LngLat(verticesCoords[i][0], verticesCoords[i][1]);
        }
        return vertices;
    }

    public LngLat[] getVertices() {
        return vertices;
    }
}
