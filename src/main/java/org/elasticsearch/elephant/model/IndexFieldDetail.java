package org.elasticsearch.elephant.model;

import java.util.Collection;
import java.util.Collections;

public class IndexFieldDetail {

    private final String indexName;
    private final Collection<String> fields;

    public IndexFieldDetail(String indexName, Collection<String> fields) {
        this.indexName = indexName;
        this.fields = Collections.unmodifiableCollection(fields);
    }


    public String getIndexName() {
        return indexName;
    }

    public Collection<String> getFields() {
        return fields;
    }
}
