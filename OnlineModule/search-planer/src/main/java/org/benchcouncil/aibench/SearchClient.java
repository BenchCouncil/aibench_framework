package org.benchcouncil.aibench;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class SearchClient {

    private RestHighLevelClient client;

    public SearchClient(String host, int port, String schema) {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, schema)));
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }
}
