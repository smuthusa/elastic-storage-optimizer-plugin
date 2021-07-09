package org.elasticsearch.plugin.elephant;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.IndicesProvider;
import org.elasticsearch.index.exception.MappingAlreadyFoundException;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StorageConfigRestAction extends BaseRestHandler {

    public static final String STORAGE_OPTIMIZER = "/_storage/_optimize";

    private final IndicesProvider indicesProvider;

    public StorageConfigRestAction(IndicesProvider indicesProvider) {
        this.indicesProvider = indicesProvider;
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) {
        logger.info(String.format("Handling request .....%s <<-->>> %s >>> %s", restRequest.uri(), restRequest.method(), restRequest.getHttpRequest()));
        if (restRequest.method() == RestRequest.Method.GET) {
            return (RestChannelConsumer) AccessController.doPrivileged((PrivilegedAction<Object>) this::getStorageOptimizedIndices);
        } else if (restRequest.method() == RestRequest.Method.POST) {
            return (RestChannelConsumer) AccessController.doPrivileged((PrivilegedAction<Object>) () -> handleIndexOptimizationRequest(restRequest));
        } else {
            throw new RuntimeException("Not found!");
        }
    }

    @Override
    public String getName() {
        return "storage_optimizer";
    }

    @Override
    public List<Route> routes() {
        logger.info(String.format("Registering routes for '%s'", STORAGE_OPTIMIZER));
        return Arrays.asList(
                new Route(RestRequest.Method.GET, STORAGE_OPTIMIZER),
                new Route(RestRequest.Method.POST, STORAGE_OPTIMIZER)
        );
    }

    private RestChannelConsumer handleIndexOptimizationRequest(RestRequest request) {
        Function<XContentBuilder, BytesRestResponse> handler = (contentBuilder) -> {
            try {
                Map<String, Object> values = XContentHelper.convertToMap(request.requiredContent(), false, request.getXContentType()).v2();
                indicesProvider.addIndexToOptimization(values);
                contentBuilder.startObject().field("message", "Successfully updated!").endObject();
                return new BytesRestResponse(RestStatus.OK, contentBuilder);
            } catch (IOException | MappingAlreadyFoundException e) {
                return new BytesRestResponse(RestStatus.BAD_REQUEST, contentBuilder);
            }
        };

        return handleRequest(handler);
    }

    private RestChannelConsumer getStorageOptimizedIndices() {
        Function<XContentBuilder, BytesRestResponse> handler = contentBuilder -> {
            Map<String, Collection<String>> indices = indicesProvider.getEnabledIndices();
            try {
                contentBuilder.startObject().field("indices", indices).endObject();
                return new BytesRestResponse(RestStatus.OK, contentBuilder);
            } catch (IOException e) {
                return new BytesRestResponse(RestStatus.BAD_REQUEST, contentBuilder);
            }
        };
        return handleRequest(handler);
    }

    private RestChannelConsumer handleRequest(Function<XContentBuilder, BytesRestResponse> requestHandler) {
        return (channel) -> {
            try {
                XContentBuilder builder = channel.newBuilder();
                BytesRestResponse response = requestHandler.apply(builder);
                channel.sendResponse(response);
            } catch (final Exception e) {
                channel.sendResponse(new BytesRestResponse(channel, e));
            }
        };
    }
}
