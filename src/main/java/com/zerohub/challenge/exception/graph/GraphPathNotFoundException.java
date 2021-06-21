package com.zerohub.challenge.exception.graph;

public class GraphPathNotFoundException extends RuntimeException {

    public GraphPathNotFoundException(String fromVertex, String toVertex, Throwable cause) {
        super(String.format("Path from %s to %s was not found", fromVertex, toVertex), cause);
    }

}
