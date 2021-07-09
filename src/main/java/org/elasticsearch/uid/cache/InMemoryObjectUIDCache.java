package org.elasticsearch.uid.cache;

import java.util.Arrays;

public class InMemoryObjectUIDCache implements ObjectUIDCache {

    @Override
    public Object transform(String field, Object value) {
        return "10001";//Arrays.asList(10001, 20002, 30003);
    }
}
