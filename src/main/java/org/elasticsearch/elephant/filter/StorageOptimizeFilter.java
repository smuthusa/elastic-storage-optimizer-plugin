package org.elasticsearch.elephant.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.action.support.replication.ReplicationTask;
import org.elasticsearch.index.DocumentFieldTransformer;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StorageOptimizeFilter implements ActionFilter {
    private static final Logger logger = LogManager.getLogger(StorageOptimizeFilter.class);

    private final DocumentFieldTransformer documentFieldTransformer;

    public StorageOptimizeFilter(DocumentFieldTransformer documentFieldTransformer) {
        this.documentFieldTransformer = documentFieldTransformer;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply(Task task, String action, Request request, ActionListener<Response> listener, ActionFilterChain<Request, Response> chain) {
        boolean intercept = task instanceof ReplicationTask && request instanceof IndexRequest;
        logger.debug(String.format(" %s >>>>>>>>> %s >>>> %s >>> %s :::: %s >>> %s >>> :::: %s >>>  %s ::::  %s >>> %s",
                intercept, task.getAction(), request.toString(), task.getClass(),
                action.getClass(), action, listener.getClass(), listener,
                chain.getClass(), chain));
        if (intercept) {
            IndexRequest indexReq = (IndexRequest) request;
            documentFieldTransformer.transform(indexReq);
        } else if (request instanceof SearchRequest && task.getAction().equals("indices:data/read/search")) {
            SearchRequest searchRequest = (SearchRequest)request;
            QueryBuilder query = searchRequest.source().query();
            if (searchRequest.indices().length > 1) {
                throw new RuntimeException("Multi index search is not supported!");
            }
            String index = searchRequest.indices()[0];
            if (query instanceof BoolQueryBuilder) {
                BoolQueryBuilder booleanQuery = (BoolQueryBuilder) query;
                List<QueryBuilder> filters = booleanQuery.filter();
                Collection<QueryBuilder> transformedQueryBuilder = new ArrayList<>();
                filters.forEach(filter -> {
                    Collection<String> fields = documentFieldTransformer.interceptSearch(index);
                    if (filter instanceof TermQueryBuilder) {
                        TermQueryBuilder tqb = (TermQueryBuilder) filter;
                        String value = tqb.value().toString();
                        TermQueryBuilder qb = new TermQueryBuilder(tqb.fieldName(), "10001");
                        transformedQueryBuilder.add(qb);
                        logger.error("Intercepting....");
                    } else {
                        transformedQueryBuilder.add(filter);
                    }
                });
                filters.clear();
                filters.addAll(transformedQueryBuilder);
            }
        }
        chain.proceed(task, action, request, listener);
    }

    @Override
    public int order() {
        return 0;
    }
}