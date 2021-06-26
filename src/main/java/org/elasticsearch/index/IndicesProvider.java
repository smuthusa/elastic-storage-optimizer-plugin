package org.elasticsearch.index;

import org.elasticsearch.index.exception.MappingAlreadyFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class IndicesProvider {
    private final Map<String, Collection<String>> indicesFieldMap;

    public IndicesProvider() {
        indicesFieldMap = new TreeMap<>();
    }

    public boolean canOptimize(String indexName) {
        return indicesFieldMap.keySet().stream().anyMatch(indexName::startsWith);
    }

    public Collection<String> fieldsToOptimize(String indexName) {
        return getFields(indexName);
    }

    private Collection<String> getFields(String indexName) {
        for (Map.Entry<String, Collection<String>> entry : indicesFieldMap.entrySet()) {
            if (indexName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    public void addIndexToOptimization(Map<String, Object> indexDetail) throws MappingAlreadyFoundException {
        String indexName = indexDetail.get("index").toString();
        Collection<String> fields = (Collection<String>) indexDetail.get("fields");
        if (canOptimize(indexName)) {
            throw new MappingAlreadyFoundException(String.format("Mapping already found for %s", indexName));
        }
        indicesFieldMap.computeIfAbsent(indexName, (key) -> fields);
    }

    public Map<String, Collection<String>> getEnabledIndices() {
        return Collections.unmodifiableMap(indicesFieldMap);
    }
}
