package com.zerohub.challenge.exception.graph;

public class VertexNotFoundException extends RuntimeException {

    private final String notExistedVertex;

    public VertexNotFoundException(String notExistedVertex, Throwable cause) {
        super("Try to work with not existed vertex " + notExistedVertex, cause);
        this.notExistedVertex = notExistedVertex;
    }

    public String getNotExistedVertex() {
        return notExistedVertex;
    }
}
