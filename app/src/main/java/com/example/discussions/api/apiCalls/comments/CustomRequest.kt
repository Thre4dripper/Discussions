package com.example.discussions.api.apiCalls.comments

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import org.json.JSONArray
import org.json.JSONObject


open class CustomRequest(
    method: Int,
    url: String,
    jsonRequest: JSONObject,
    listener: Response.Listener<JSONArray>,
    errorListener: Response.ErrorListener
) : JsonRequest<JSONArray>(method, url, jsonRequest.toString(), listener, errorListener) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONArray> {
        return try {
            val jsonString =
                String(response!!.data, charset(HttpHeaderParser.parseCharset(response.headers)))
            Response.success(JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }
}