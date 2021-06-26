package org.elephant.elasticsearch.plugin;

import org.elasticsearch.client.Request;
import org.elasticsearch.plugin.elephant.StorageConfigRestAction;
import org.elasticsearch.test.ESIntegTestCase;

public class StorageOptimizerPluginTest extends ESIntegTestCase {

    public void testStorageEndpoint() throws Exception {
        Request request = new Request("GET", StorageConfigRestAction.STORAGE_OPTIMIZER);
        //Response response = client().performRequest(request);
        //System.out.println(response);
    }
}