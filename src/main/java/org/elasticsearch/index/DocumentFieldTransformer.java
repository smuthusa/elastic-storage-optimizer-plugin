package org.elasticsearch.index;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.exception.FieldValueMissingException;
import org.elasticsearch.uid.cache.ObjectValueToIntegerMapper;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DocumentFieldTransformer {
    private final IndicesProvider indicesProvider;
    private final ObjectValueToIntegerMapper objectValueToIntegerMapper;

    public DocumentFieldTransformer(IndicesProvider indicesProvider, ObjectValueToIntegerMapper objectValueToIntegerMapper) {
        this.indicesProvider = indicesProvider;
        this.objectValueToIntegerMapper = objectValueToIntegerMapper;
    }

    public void transform(IndexRequest indexReq) {
        if (indicesProvider.canOptimize(indexReq.index())) {
            Map<String, Object> data = indexReq.sourceAsMap();
            Collection<String> fieldsToTransform = indicesProvider.fieldsToOptimize(indexReq.index());
            fieldsToTransform.forEach(field -> transformField(field, indexReq, data));
        }
    }

    public Optional<Integer> getValueOnDisk(String index, String fieldName, String value) {
        boolean intercept = indicesProvider.canOptimize(index);
        if (intercept) {
            Collection<String> fields = indicesProvider.fieldsToOptimize(index);
            if (fields.contains(fieldName)) {
                Optional<Integer> transformedValue = objectValueToIntegerMapper.transform(value);
                transformedValue.or(() -> {
                    String message = String.format("Index [%s] field [%s] value [%s] missing in cache", index, fieldName, value);
                    throw new FieldValueMissingException(message); //TODO send proper response to the user
                });
                return transformedValue;
            }
        }
        return Optional.empty();
    }

    private void transformField(String field, IndexRequest indexReq, Map<String, Object> data) {
        Object value = data.remove(field);
        if (value != null) {
            Object newValue = objectValueToIntegerMapper.transform(field, value.toString());
            data.put(field, newValue);
            indexReq.source(data);
        }
    }
}