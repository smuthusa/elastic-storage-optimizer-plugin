package org.elasticsearch.index;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.uid.cache.ObjectUIDCache;

import java.util.Collection;
import java.util.Map;

public class DocumentFieldTransformer {
    private final IndicesProvider indicesProvider;
    private final ObjectUIDCache objectUIDCache;

    public DocumentFieldTransformer(IndicesProvider indicesProvider, ObjectUIDCache objectUIDCache) {
        this.indicesProvider = indicesProvider;
        this.objectUIDCache = objectUIDCache;
    }

    public void transform(IndexRequest indexReq) {
        if (indicesProvider.canOptimize(indexReq.index())) {
            Map<String, Object> data = indexReq.sourceAsMap();
            Collection<String> fieldsToTransform = indicesProvider.fieldsToOptimize(indexReq.index());
            fieldsToTransform.forEach(field -> transformField(field, indexReq, data));
        }
    }

    private void transformField(String field, IndexRequest indexReq, Map<String, Object> data) {
        Object value = data.remove(field);
        if (value != null) {
            Object newValue = objectUIDCache.transform(field, value);
            data.put(field, newValue);
            indexReq.source(data);
        }
    }
}