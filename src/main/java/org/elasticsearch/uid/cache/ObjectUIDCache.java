package org.elasticsearch.uid.cache;

import java.util.Optional;

public interface ObjectUIDCache<T> {

    Integer transform(String field, String value);

    Optional<T> transform(String value);
}
