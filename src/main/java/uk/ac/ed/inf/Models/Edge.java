package uk.ac.ed.inf.Models;


import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public record Edge(LngLat start, LngLat end) {
    public boolean intersectsArea(Area area) {
        // generate list of edges from vertices
        LngLat[] vertices = area.getVertices();
        Set<LngLat> verticesSet = new HashSet<>(Arrays.asList(vertices));
        // set of edges
        Set<Edge> edges = new HashSet<>();
        for (int i = 0; i < vertices.length - 1; i++) {
            edges.add(new Edge(vertices[i], vertices[(i + 1)]));
        }
        // check if any of the edges intersect
        for (Edge edge : edges) {
            if (this.intersectsEdge(edge)
                    && !this.start.equals(edge.start)
                    && !this.start.equals(edge.end)
                    && !this.end.equals(edge.start)
                    && !this.end.equals(edge.end)) {
                return true;
            }
        }
        if (Arrays.stream(vertices).noneMatch(v -> v.equals(this.start) || v.equals(this.end))) {
            return false;
        }
        // TODO: fix for concave polygons (or convex hull if that fails..)
        return verticesSet.contains(this.start)
                && verticesSet.contains(this.end)
                && edges.stream()
                .noneMatch(e -> (e.start.equals(this.start) && e.end.equals(this.end))
                        || (e.start.equals(this.end) && e.end.equals(this.start)));
    }

    public boolean intersectsEdge(Edge edge) {
        return Line2D.linesIntersect(this.start.lng(), this.start.lat(),
                                     this.end.lng(), this.end.lat(),
                                     edge.start.lng(), edge.start.lat(),
                                     edge.end.lng(), edge.end.lat());
    }

}

