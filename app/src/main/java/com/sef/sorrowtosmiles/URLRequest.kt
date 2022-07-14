package com.sef.sorrowtosmiles

import android.R.attr
import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache

import com.android.volley.toolbox.*
import org.json.JSONObject
import android.R.attr.bitmap
import android.support.v4.os.IResultReceiver

import android.widget.Toast
import androidx.test.core.app.ApplicationProvider

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.android.volley.*

import org.json.JSONException


/**
 * Handles URL Requests, including POST and GET requests. Uses Volley
 */
class URLRequest {
    companion object {

        const val serverURL = "https://sorrow-to-smiles.glitch.me/"
        const val uploadURL = serverURL + "upload"
        const val uploadsURL = serverURL + "uploads"
        const val serverTelegramURL = serverURL + "telegram"
        const val serverTelegramWaitURL = "$serverTelegramURL/wait"

        const val googleTokenRequest = "https://www.googleapis.com/oauth2/v4/token"

        const val telegramURL = "https://telegram.me/SorrowToSmiles_bot?start="

        private lateinit var instance: URLRequest

        /**
         * Initialize the URL Request object
         */
        fun initialize(context: Context){
            instance = URLRequest()
            instance.requestQueue = Volley.newRequestQueue(context)
        }

        /**
         * Make a GET Request
         */
        fun requestGET(url: String, onSuccess: (response: String) -> Unit, onFailure: (err: VolleyError) -> Unit){
            instance.requestGET(url, onSuccess, onFailure)
        }

        /**
         * Make a POST Request
         */
        fun jsonPOSTRequest(url: String, json: JSONObject, onSuccess: (response: JSONObject) -> Unit, onFailure: (err: VolleyError) -> Unit){
            instance.jsonPOSTRequest(url, json, onSuccess, onFailure)
        }

        /**
         * Make a POST Request with custom timeout
         */
        fun jsonPOSTRequest(url: String, json: JSONObject, onSuccess: (response: JSONObject) -> Unit, onFailure: (err: VolleyError) -> Unit, timeout: Int){
            instance.jsonPOSTRequest(url, json, onSuccess, onFailure, timeout)
        }
    }

    private lateinit var requestQueue: RequestQueue

    /**
     * Makes a GET request
     */
    private fun requestGET(url: String, onSuccess: (response: String) -> Unit, onFailure: (err: VolleyError) -> Unit){
        val strReq = StringRequest(Request.Method.GET, url,
            {
                response ->
                    onSuccess(response)
            },
            {
                err ->
                    onFailure(err)
            }
        )
        requestQueue.add(strReq)
    }

    /**
     * Makes a POST Request with default timeout
     */
    private fun jsonPOSTRequest(url: String, json: JSONObject, onSuccess: (response: JSONObject) -> Unit, onFailure: (err: VolleyError) -> Unit){
        jsonPOSTRequest(url, json, onSuccess, onFailure, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS)
    }

    /**
     * Makes a POST Request with custom timeout
     */
    private fun jsonPOSTRequest(url: String, json: JSONObject, onSuccess: (response: JSONObject) -> Unit, onFailure: (err: VolleyError) -> Unit, timeoutTime: Int){
        val strReq = JsonObjectRequest(Request.Method.POST, url, json,
            {
                    response ->
                onSuccess(response)
            },
            {
                    err ->
                onFailure(err)
            }
        )
        strReq.retryPolicy = DefaultRetryPolicy(timeoutTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(strReq)
    }

}