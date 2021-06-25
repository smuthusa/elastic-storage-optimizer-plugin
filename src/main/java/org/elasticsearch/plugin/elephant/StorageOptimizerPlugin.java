package org.elasticsearch.plugin.elephant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.action.support.replication.ReplicationTask;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.tasks.Task;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StorageOptimizerPlugin extends Plugin implements ActionPlugin {

    public static final Logger logger = LogManager.getLogger(StorageOptimizerPlugin.class);

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {
        return Collections.singletonList(new StorageConfigRestAction());
    }

    @Override
    public List<ActionFilter> getActionFilters() {

        ActionFilter filter = new ActionFilter() {
            @Override
            public int order() {
                return 0;
            }

            @Override
            public <Request extends ActionRequest, Response extends ActionResponse> void apply(Task task, String action, Request request, ActionListener<Response> actionListener, ActionFilterChain<Request, Response> actionFilterChain) {
                if (task instanceof ReplicationTask) {
                    if (request instanceof IndexRequest) {
                        IndexRequest indexReq = (IndexRequest) request;
                        Map<String, Object> data = indexReq.sourceAsMap();
                        data.remove("tags");
                        data.put("tags", Arrays.asList(10001, 10002));
                        indexReq.source(data);
                        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> modified data " + indexReq.sourceAsMap());
                    } else {
                        logger.info(">>>>>>>>>>>>> Request not intercepted!" + task.getAction() + "------" + request.toString());
                    }
                } else {
                    logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> " + task.getAction() + "------" + request.toString() + " " + task.getClass() +
                            "action.getClass() " + action.getClass() + " --- " + action + "actionListener.getClass() " + actionListener.getClass() + "----" + actionListener +
                            "actionFilterChain.getClass() " + actionFilterChain.getClass() + "----" + actionFilterChain);
                }
                actionFilterChain.proceed(task, action, request, actionListener);
            }
        };
        return Collections.singletonList(filter);
    }
}
