package org.elasticsearch.index.exception;

public class FieldValueMissingException extends IllegalStateException {
    public FieldValueMissingException(String message) {
        super(message);
    }
}
