package com.example.mrakopediareader.api;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

class Queue {
    // Three minutes for super-long queries
    private static int TIMEOUT = 1000 * 60 * 3;

    private static RetryPolicy retryPolicy = new RetryPolicy() {
        @Override
        public int getCurrentTimeout() {
            return TIMEOUT;
        }

        @Override
        public int getCurrentRetryCount() {
            return TIMEOUT;
        }

        @Override
        public void retry(VolleyError error) {}
    };

    private final RequestQueue requestQueue;

    public Queue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    private void queueRequest(JsonArrayRequest request) {
        request.setRetryPolicy(retryPolicy);
        this.requestQueue.add(request);
    }

    private void queueRequest(JsonObjectRequest request) {
        request.setRetryPolicy(retryPolicy);
        this.requestQueue.add(request);
    }

    public void jsonObjectRequest(
        String url,
        Response.Listener<JSONObject> listener,
        Response.ErrorListener errorListener
    ) {
        queueRequest(new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener));
    }

    public void jsonArrayRequest(
        String url,
        Response.Listener<JSONArray> listener,
        Response.ErrorListener errorListener
    ) {
        queueRequest(new JsonArrayRequest(url, listener, errorListener));
    };
}
