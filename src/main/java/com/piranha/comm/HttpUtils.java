package com.piranha.comm;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

/**
 * Created by root on 4/14/16.
 */
public class HttpUtils {
    public static HttpResponse doRequest(HttpUriRequest request) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        return client.execute(request);
    }
}
