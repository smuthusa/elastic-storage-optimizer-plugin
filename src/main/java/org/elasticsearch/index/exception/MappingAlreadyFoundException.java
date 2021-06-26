package org.elasticsearch.index.exception;

public class MappingAlreadyFoundException extends Exception {
    public MappingAlreadyFoundException(String message) {
        super(message);
    }
}
