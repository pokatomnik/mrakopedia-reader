package com.example.mrakopediareader.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.reactivex.rxjava3.core.Observable
import org.json.JSONArray
import org.json.JSONObject


class Queue(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    private val retryPolicy: RetryPolicy = MRRetryPolicy()

    private fun queueRequest(request: JsonArrayRequest) {
        request.retryPolicy = retryPolicy
        requestQueue.add(request)
    }

    private fun queueRequest(request: JsonObjectRequest) {
        request.retryPolicy = retryPolicy
        requestQueue.add(request)
    }

    fun jsonObjectRequestGET(url: String): Observable<JSONObject> {
        return Observable.create { resolver ->
            val request = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { resolver.onNext(it); resolver.onComplete() },
                { resolver.onError(it); resolver.onComplete() }
            )
            queueRequest(request)
        }
    }

    fun jsonArrayRequestGET(
        url: String
    ): Observable<JSONArray> {
        return Observable.create {
            resolver ->
                val request = JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    { resolver.onNext(it); resolver.onComplete() },
                    { resolver.onError(it); resolver.onComplete() }
                )
                queueRequest(request)
        }
    }
}