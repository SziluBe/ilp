package uk.ac.ed.inf.Models.Input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import uk.ac.ed.inf.Models.LngLat;

/**
 * Represents an area using its vertices.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Area(LngLat[] vertices) {
    /**
     * Default record constructor.
     *
     * @param LngLat[] vertices The vertices of the area.
     */

    /**
     * Creates an area from a 2D array of doubles.
     * This is used for deserialization.
     *
     * @param verticesCoords The 2D array of doubles; each inner array should have length 2, and represent a vertex.
     */
    @JsonCreator
    private Area(@JsonProperty("coordinates") Double[][] verticesCoords) {
        this(Area.from2dDoubleArray(verticesCoords));
    }

    @NotNull
    private static LngLat[] from2dDoubleArray(Double[][] verticesCoords) {
        if (verticesCoords == null) {
            return new LngLat[0];
        }
        var vertices = new LngLat[verticesCoords.length];
        for (int i = 0; i < verticesCoords.length; i++) {
            vertices[i] = new LngLat(verticesCoords[i][0], verticesCoords[i][1]);
        }
        return vertices;
    }

    /**
     * Gets the vertices of the area.
     * Never returns null. If the area has no vertices, returns an empty array.
     *
     * @return The vertices of the area.
     */
    @NotNull
    public LngLat[] getVertices() {
        return vertices;
    }
}
