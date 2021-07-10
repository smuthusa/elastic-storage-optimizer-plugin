package org.elasticsearch.elephant.query.interceptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.DocumentFieldTransformer;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchQueryInterceptor implements QueryInterceptor<SearchRequest> {

    private static final Logger logger = LogManager.getLogger(SearchQueryInterceptor.class);

    private final DocumentFieldTransformer documentFieldTransformer;

    public SearchQueryInterceptor(DocumentFieldTransformer documentFieldTransformer) {
        this.documentFieldTransformer = documentFieldTransformer;
    }

    @Override
    public void interceptQuery(SearchRequest searchRequest) {
        QueryBuilder query = searchRequest.source().query();
        if (searchRequest.indices().length > 1) {
            throw new RuntimeException("Multi index search is not supported!");
        }
        String index = searchRequest.indices()[0];
        if (query instanceof BoolQueryBuilder) {
            logger.debug(String.format("Intercepting SearchRequest on Index %s", index));
            BoolQueryBuilder booleanQuery = (BoolQueryBuilder) query;
            List<QueryBuilder> filters = booleanQuery.filter();
            List<QueryBuilder> transformedQueryBuilder = filters.stream()
                    .map(filter -> transformTermQuery(index, filter))
                    .collect(Collectors.toList());
            filters.clear(); //TODO Not a cleaner approach
            filters.addAll(transformedQueryBuilder);
        }
    }

    private QueryBuilder transformTermQuery(String index, QueryBuilder filter) {
        if (filter instanceof TermQueryBuilder) {
            TermQueryBuilder tqb = (TermQueryBuilder) filter;
            String value = tqb.value().toString();
            Optional<Integer> valueOnDisk = documentFieldTransformer.getValueOnDisk(index, tqb.fieldName(), value);
            if (valueOnDisk.isEmpty()) {
                return filter;
            }
            logger.trace(String.format("[%s] Translated user input '%s' to value on Disk '%s'", index, value, valueOnDisk.get()));
            return new TermQueryBuilder(tqb.fieldName(), valueOnDisk.get());
        }
        return filter;
    }
}
