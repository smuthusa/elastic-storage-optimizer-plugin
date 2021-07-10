package org.elasticsearch.elephant.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.action.support.replication.ReplicationTask;
import org.elasticsearch.elephant.query.interceptor.QueryInterceptor;
import org.elasticsearch.index.DocumentFieldTransformer;
import org.elasticsearch.tasks.Task;

import java.util.Arrays;

public class StorageOptimizeFilter implements ActionFilter {
    private static final Logger logger = LogManager.getLogger(StorageOptimizeFilter.class);

    private final DocumentFieldTransformer documentFieldTransformer;
    private final QueryInterceptor<SearchRequest> searchInterceptor;

    public StorageOptimizeFilter(DocumentFieldTransformer documentFieldTransformer, QueryInterceptor<SearchRequest> searchInterceptor) {
        this.documentFieldTransformer = documentFieldTransformer;
        this.searchInterceptor = searchInterceptor;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply(Task task, String action, Request request, ActionListener<Response> listener, ActionFilterChain<Request, Response> chain) {
        if (task instanceof ReplicationTask && request instanceof IndexRequest) {
            IndexRequest indexReq = (IndexRequest) request;
            documentFieldTransformer.transform(indexReq);
        } else if (request instanceof SearchRequest) {
            SearchRequest searchRequest = (SearchRequest) request;
            logger.debug(String.format("Intercepting search request, Index [%s]", Arrays.toString(searchRequest.indices())));
            searchInterceptor.interceptQuery(searchRequest);
        }
        chain.proceed(task, action, request, listener);
    }

    @Override
    public int order() {
        return 0;
    }
}