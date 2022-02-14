package com.example.mrakopediareader.api

import com.android.volley.RetryPolicy
import com.android.volley.VolleyError

internal class MRRetryPolicy : RetryPolicy {
    override fun getCurrentTimeout(): Int {
        return TIMEOUT
    }

    override fun getCurrentRetryCount(): Int {
        return TIMEOUT
    }

    override fun retry(error: VolleyError?) {
        // Do nothing
    }

    companion object {
        const val TIMEOUT = 1000 * 60 * 3
    }
}