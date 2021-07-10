package org.elephant.elasticsearch.plugin;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.core.internal.io.IOUtils;
import org.junit.*;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StorageOptimizerPluginTest {

    private RestClient client;

    private final Request getOptimizedIndicesRequest = new Request("GET", "/_storage/_optimize");

    @Before
    public void init() {
        client = RestClient.builder(new HttpHost("127.0.0.1", 9201)).build();
    }

    @After
    public void destroy() throws IOException {
        IOUtils.close(client);
    }

    @Test
    public void testEnableIndexOptimization() throws Exception {
        Response response = client.performRequest(getOptimizedIndicesRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String optimizedIndices = EntityUtils.toString(response.getEntity());
        assertEquals("{\"indices\":{}}", optimizedIndices);

        optimizeIndex("statistics", new String[]{"hostname"});

        response = client.performRequest(getOptimizedIndicesRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
        optimizedIndices = EntityUtils.toString(response.getEntity());
        assertEquals("{\"indices\":{\"statistics\":[\"hostname\"]}}", optimizedIndices);
    }

    @Test
    public void verifyDocumentFieldIsTranslatedToInteger() throws IOException {
        String indexName = "stats_20210601";
        optimizeIndex(indexName, new String[]{"region"});
        addTimeSeriesRecord(indexName);
        Request search = new Request("POST", indexName + "/_search");
        search.setJsonEntity("{\"query\":{\"bool\":{\"filter\":{\"term\":{\"region\":\"London\"}}}}}");
        Response searchResponse = client.performRequest(search);
        assertEquals(200, searchResponse.getStatusLine().getStatusCode());
        String searchRes = EntityUtils.toString(searchResponse.getEntity());
        String expected = "{\"hostname\":\"madelephant\",\"datetime\":\"2021-01-15T14:12:12\",\"cpu\":\"40.0\",\"region\":1}";
        assertTrue(searchRes.contains(expected));
    }

    @Test(expected = ResponseException.class)
    public void verifyErrorThrownWhenValueNotFoundInCache() throws IOException {
        String indexName = "stats_20210602";
        optimizeIndex(indexName, new String[]{"region"});
        addTimeSeriesRecord(indexName);
        Request search = new Request("POST", indexName + "/_search");
        search.setJsonEntity("{\"query\":{\"bool\":{\"filter\":{\"term\":{\"region\":\"Chennai\"}}}}}");
        try {
            client.performRequest(search);
        } catch (ResponseException e) {
            assertEquals(500, e.getResponse().getStatusLine().getStatusCode());
            String searchRes = EntityUtils.toString(e.getResponse().getEntity());
            String expected = "Index [" + indexName + "] field [region] value [Chennai] missing in cache";
            assertTrue(searchRes.contains(expected));
            throw e;
        }
    }

    private void addTimeSeriesRecord(String indexName) throws IOException {
        Request post = new Request("POST", indexName + "/_doc");
        post.setJsonEntity("{\"hostname\":\"madelephant\",\"datetime\":\"2021-01-15T14:12:12\",\"cpu\":\"40.0\",\"region\":\"London\"}");
        Response response = client.performRequest(post);
        assertEquals(201, response.getStatusLine().getStatusCode());
        refreshIndex(indexName);
    }


    private void optimizeIndex(String index, String[] docFields) throws IOException {
        String fields = Stream.of(docFields).map(s -> "\"" + s + "\"").collect(Collectors.joining());
        Request optimizeIndexRequest = new Request("PUT", "/_storage/_optimize");
        String entity = "{\"index\": \"" + index + "\", \"fields\": [" + fields + "]}";
        optimizeIndexRequest.setJsonEntity(entity);
        Response putResponse = client.performRequest(optimizeIndexRequest);
        assertEquals(200, putResponse.getStatusLine().getStatusCode());
    }

    private void refreshIndex(String indexName) throws IOException {
        Request post = new Request("POST", indexName + "/_refresh");
        Response response = client.performRequest(post);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }
}