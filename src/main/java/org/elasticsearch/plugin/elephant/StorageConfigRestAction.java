package org.elasticsearch.plugin.elephant;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import java.util.Arrays;
import java.util.List;

public class StorageConfigRestAction extends BaseRestHandler {

    public static final String OPEN_DISTRO_STORAGE_OPTIMIZER = "/_opendistro/_storage/_optimizer";

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) {
        logger.info(String.format("Handling request .....%s <<-->>> %s", restRequest.uri(), restRequest.method()));
        if (restRequest.method() == RestRequest.Method.GET) {
            return handleRequest(restRequest.method().name());
        } else if (restRequest.method() == RestRequest.Method.POST) {
            return handleRequest(restRequest.method().name());
        } else {
            throw new RuntimeException("Not found!");
        }
    }

    private RestChannelConsumer handleRequest(String request) {
        return (channel) -> {
            try {
                XContentBuilder builder = channel.newBuilder();
                builder.startObject().field(request, " Will be implemented soon!\n").endObject();
                channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
            } catch (final Exception e) {
                channel.sendResponse(new BytesRestResponse(channel, e));
            }
        };
    }

    @Override
    public String getName() {
        return "opendistro_storage_optimizer";
    }

    @Override
    public List<Route> routes() {
        logger.info(String.format("Registering routes for '%s'", OPEN_DISTRO_STORAGE_OPTIMIZER));
        return Arrays.asList(
                new Route(RestRequest.Method.GET, OPEN_DISTRO_STORAGE_OPTIMIZER),
                new Route(RestRequest.Method.POST, OPEN_DISTRO_STORAGE_OPTIMIZER)
        );
    }
}
