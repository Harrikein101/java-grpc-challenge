package com.zerohub.challenge.graph;

import com.zerohub.challenge.exception.graph.GraphPathNotFoundException;
import com.zerohub.challenge.utils.DecimalUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;

public class DirectedWeightedGraphWithPreprocessing extends DirectedWeightedGraph {

    @Data
    private static class PathInfo {
        private final BigDecimal pathLength;
        private final int edgesCount;
    }

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, PathInfo>> preprocessedPaths
            = new ConcurrentHashMap<>();

    @Override
    public synchronized void addEdge(String vertexA, String vertexB, BigDecimal weightAB) {
        if (!existedEdgeWithWeight(vertexA, vertexB, weightAB)) {
            addVertex(vertexA);
            addVertex(vertexB);
            addTwoWeightedEdges(vertexA, vertexB, weightAB);
            countPathsDynamically(vertexA, vertexB, weightAB);
        }
    }

    @Override
    public BigDecimal findPath(String vertexA, String vertexB) {
        checkExistedVertex(vertexA);
        checkExistedVertex(vertexB);
        PathInfo path = preprocessedPaths.get(vertexA).get(vertexB);
        if (path == null) {
            throw new GraphPathNotFoundException(vertexA, vertexB, null);
        }
        return path.pathLength;
    }

    /**
     * Adds new vertex. Also adds path with loop to the vertex (path from vertex to itself is always 1)
     *
     * @param vertex vertex id
     */
    @Override
    protected void addVertex(String vertex) {
        super.addVertex(vertex);
        preprocessedPaths.putIfAbsent(vertex, new ConcurrentHashMap<>());
        preprocessedPaths.get(vertex).put(vertex, new PathInfo(BigDecimal.ONE, 0));
    }

    /**
     * After each adding an edge this graph always counts all paths between all vertexes.
     * This algorithm is dynamic and uses previous results of counting path distances,
     * so it works quite fast with O(V*V) difficulty - but it can be optimized in future.
     *
     * @param vertexA  first vertex of the edge that was added
     * @param vertexB  second vertex of the edge that was added
     * @param weightAB weight of added edge
     */
    private void countPathsDynamically(String vertexA, String vertexB, BigDecimal weightAB) {
        ConcurrentHashMap<String, PathInfo> pathsFromA =
                preprocessedPaths.getOrDefault(vertexA, new ConcurrentHashMap<>());
        ConcurrentHashMap<String, PathInfo> pathsFromB =
                preprocessedPaths.getOrDefault(vertexB, new ConcurrentHashMap<>());
        for (var pathA : pathsFromA.entrySet()) {
            for (var pathB : pathsFromB.entrySet()) {
                addPath(pathA.getKey(),
                        pathB.getKey(),
                        DecimalUtils.getReverseNumber(pathA.getValue().pathLength)
                                .multiply(pathB.getValue().pathLength, MathContext.DECIMAL64)
                                .multiply(weightAB, MathContext.DECIMAL64),
                        pathA.getValue().getEdgesCount() + pathB.getValue().getEdgesCount() + 1);
            }
        }
        addPath(vertexA, vertexB, weightAB, 1);
    }

    /**
     * Adds new path distance to memory
     *
     * @param vertexA    first vertex of the path
     * @param vertexB    last vertex of the path
     * @param weightAB   distance of the path
     * @param edgesCount count of edges in the path
     */
    private void addPath(String vertexA, String vertexB, BigDecimal weightAB, int edgesCount) {
        if (!vertexA.equals(vertexB) &&
                (preprocessedPaths.get(vertexA) == null ||
                        preprocessedPaths.get(vertexA).get(vertexB) == null ||
                        pathShouldBeUpdated(
                                preprocessedPaths.get(vertexA).get(vertexB),
                                new PathInfo(weightAB, edgesCount)))) {
            preprocessedPaths.get(vertexA).put(vertexB,
                    new PathInfo(weightAB, edgesCount));
            preprocessedPaths.get(vertexB).put(vertexA,
                    new PathInfo(DecimalUtils.getReverseNumber(weightAB), edgesCount));
        }
    }

    /**
     * Decides should we update path distance value because of adding new edge.
     * This method always uses the shortest path.
     *
     * @param oldPath saved path before adding the edge
     * @param newPath new path that is appeared because of adding new edge
     * @return should we update new value for path distance or not
     */
    private boolean pathShouldBeUpdated(PathInfo oldPath, PathInfo newPath) {
        BigDecimal oldWeight = oldPath.pathLength;
        BigDecimal newWeight = newPath.pathLength;
        return newWeight.add(oldWeight.negate())
                .setScale(4, RoundingMode.HALF_UP)
                .compareTo(BigDecimal.ZERO) > 0;
    }

}
