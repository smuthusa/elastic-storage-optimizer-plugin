package org.elasticsearch.uid.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryAutoIncrementValueCache implements ObjectValueToIntegerMapper {
    private final Map<String, Integer> uuidValueCache;
    private final AtomicInteger idIncrementedValue;

    public InMemoryAutoIncrementValueCache() {
        uuidValueCache = new ConcurrentHashMap<>();
        idIncrementedValue = new AtomicInteger(0);
    }

    @Override
    public Integer transform(String field, String value) {
        return uuidValueCache.computeIfAbsent(value, key -> idIncrementedValue.incrementAndGet());
    }

    @Override
    public Optional<Integer> transform(String value) {
        Integer transformedValue = uuidValueCache.get(value);
        return Optional.ofNullable(transformedValue);
    }
}
