package org.elasticsearch.plugin.elephant;

import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.elephant.filter.StorageOptimizeFilter;
import org.elasticsearch.elephant.query.interceptor.SearchQueryInterceptor;
import org.elasticsearch.index.DocumentFieldTransformer;
import org.elasticsearch.index.IndicesProvider;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.uid.cache.InMemoryAutoIncrementValueCache;
import org.elasticsearch.uid.cache.ObjectValueToIntegerMapper;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class StorageOptimizerPlugin extends Plugin implements ActionPlugin {

    private final IndicesProvider provider;
    private final DocumentFieldTransformer transformer;

    public StorageOptimizerPlugin() {
        provider = new IndicesProvider();
        ObjectValueToIntegerMapper objectUIDCache = new InMemoryAutoIncrementValueCache();
        transformer = new DocumentFieldTransformer(provider, objectUIDCache);
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver resolver, Supplier<DiscoveryNodes> nodesInCluster) {
        StorageConfigRestAction action = new StorageConfigRestAction(provider);
        return Collections.singletonList(action);
    }

    @Override
    public List<ActionFilter> getActionFilters() {
        SearchQueryInterceptor searchQueryInterceptor = new SearchQueryInterceptor(transformer);
        ActionFilter filter = new StorageOptimizeFilter(transformer, searchQueryInterceptor);
        return Collections.singletonList(filter);
    }
}
