package org.elasticsearch.elephant.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.action.support.replication.ReplicationTask;
import org.elasticsearch.index.DocumentFieldTransformer;
import org.elasticsearch.tasks.Task;

public class StorageOptimizeFilter implements ActionFilter {
    private static final Logger logger = LogManager.getLogger(StorageOptimizeFilter.class);

    private final DocumentFieldTransformer documentFieldTransformer;

    public StorageOptimizeFilter(DocumentFieldTransformer documentFieldTransformer) {
        this.documentFieldTransformer = documentFieldTransformer;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply(Task task, String action, Request request, ActionListener<Response> listener, ActionFilterChain<Request, Response> chain) {
        boolean intercept = task instanceof ReplicationTask && request instanceof IndexRequest;
        if (intercept) {
            IndexRequest indexReq = (IndexRequest) request;
            documentFieldTransformer.transform(indexReq);
        }
        logger.debug(String.format(" %s >>>>>>>>> %s >>>> %s >>> %s :::: %s >>> %s >>> :::: %s >>>  %s ::::  %s >>> %s",
                intercept, task.getAction(), request.toString(), task.getClass(),
                action.getClass(), action, listener.getClass(), listener,
                chain.getClass(), chain));
        chain.proceed(task, action, request, listener);
    }

    @Override
    public int order() {
        return 0;
    }
}