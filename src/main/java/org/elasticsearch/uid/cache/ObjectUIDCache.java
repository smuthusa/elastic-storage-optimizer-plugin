package org.elasticsearch.uid.cache;

public interface ObjectUIDCache {

    Object transform(String field, Object value);
}
