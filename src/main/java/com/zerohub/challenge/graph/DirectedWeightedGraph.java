package com.zerohub.challenge.graph;

import com.zerohub.challenge.exception.graph.VertexNotFoundException;
import com.zerohub.challenge.utils.DecimalUtils;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DirectedWeightedGraph {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, BigDecimal>> graphEdges
            = new ConcurrentHashMap<>();

    /**
     * Count the path between the vertexes
     *
     * @param vertexA first vertex of the path
     * @param vertexB last vertex of the path
     * @return lenght of path
     */
    public abstract BigDecimal findPath(String vertexA, String vertexB);

    /**
     * Add new edge to the graph
     *
     * @param vertexA  first vertex of the edge
     * @param vertexB  last vertex of the edge
     * @param weightAB weight of the edge
     */
    public abstract void addEdge(String vertexA, String vertexB, BigDecimal weightAB);

    /**
     * Get all adjacent edges for the vertex
     *
     * @param vertex vertex
     * @return map of adjacent vertexes and weights.
     * @throws VertexNotFoundException when vertex is not found
     */
    protected ConcurrentHashMap<String, BigDecimal> getEdgesOfVertex(String vertex) {
        var edges = graphEdges.get(vertex);
        if (edges == null) {
            throw new VertexNotFoundException(vertex, null);
        } else {
            return edges;
        }
    }

    /**
     * Check existence of the edge with weight
     *
     * @param vertexA  first vertex of edge
     * @param vertexB  second vertex of edge
     * @param weightAB weight of edge
     * @return true if vertexes are equal and weight is equal
     */
    protected boolean existedEdgeWithWeight(String vertexA, String vertexB, BigDecimal weightAB) {
        var edges = graphEdges.get(vertexA);
        if (edges != null) {
            BigDecimal weight = edges.get(vertexB);
            if (weight != null) {
                return weightAB.compareTo(weight) == 0;
            }
        }
        return false;
    }

    protected void checkExistedVertex(String vertex) {
        var edges = graphEdges.get(vertex);
        if (edges == null) {
            throw new VertexNotFoundException(vertex, null);
        }
    }

    /**
     * Add new vertex to the graph
     *
     * @param vertex vertex id
     */
    protected void addVertex(String vertex) {
        graphEdges.putIfAbsent(vertex, new ConcurrentHashMap<>());
        graphEdges.get(vertex).put(vertex, BigDecimal.ONE);
    }

    /**
     * Add two oriented edges (single and reversed) between two vertexes
     * The weights are weightAB and 1 / weightAB.
     *
     * @param vertexA  first vertex
     * @param vertexB  second vertex
     * @param weightAB weight oriented from first and second vertex
     */
    protected void addTwoWeightedEdges(String vertexA, String vertexB, BigDecimal weightAB) {
        getEdgesOfVertex(vertexA).put(vertexB, weightAB);
        getEdgesOfVertex(vertexB).put(vertexA, DecimalUtils.getReverseNumber(weightAB));
    }

}
