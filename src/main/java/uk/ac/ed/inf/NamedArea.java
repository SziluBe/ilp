package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NamedArea {
    private final String name;
    private final LngLat[] vertices;

    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     *
     * @param name The name of the area
     * @param vertices The vertices of the area
     */
    @JsonCreator
    private NamedArea(@JsonProperty("name") String name, @JsonProperty("coordinates") Double[][] vertices) {
        this.name = name;
        this.vertices = new LngLat[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = new LngLat(vertices[i][0], vertices[i][1]);
        }
    }

    public Area getArea() {
        return new Area(vertices);
    }
}

