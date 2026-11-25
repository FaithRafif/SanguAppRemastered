package com.example.SanguAppPractice.helper

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ApiHelper {
    @SuppressLint("StaticFieldLeak")
    fun request(
        path: String,
        method: String? = "GET",
        query: Map<String, String>? = null,
        body: JSONObject? = null,
        token: String? = null,
        callback: (ok: Boolean, res: String) -> Unit,
    ) {
        object : AsyncTask<Void, Void, Pair<Boolean, String>>() {
            override fun doInBackground(vararg params: Void?): Pair<Boolean, String> {
                return try{
                    val base = "http://10.28.25.214:5000/api$path"
                    val fullUrl = if(query.isNullOrEmpty()){
                        base
                    }
                    else {
                        val qs = query.entries.joinToString("&") { (k,  v) ->
                            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
                        }
                        "$base?$qs"
                    }

                    val url = URL(fullUrl)
                    (url.openConnection() as HttpURLConnection).run {
                        connectTimeout = 10000
                        readTimeout = 10000
                        requestMethod = method
                        setRequestProperty("Content-Type", "application/json")
                        if (!token.isNullOrEmpty() && token.isNotBlank()) {
                            setRequestProperty("Authorization", "Bearer $token")
                        }
                        body?.let{

                            doOutput = true
                            outputStream.write(body.toString().toByteArray())
                        }

                        val success = responseCode in 200..299
                        val stream = if (success) inputStream else errorStream
                        val text = stream.bufferedReader().readText()
                        Log.d("Request Success", text)
                        Pair(success, text)
                    }

                }catch(e: Exception) {
                    Log.d("RequestError", e.message ?: "Unknown Error")
                    Pair(false, e.message ?: "Unknown Error")
                }
            }

            override fun onPostExecute(result: Pair<Boolean, String>) {
                super.onPostExecute(result)
                callback(result.first, result.second)
            }

        }.execute()
    }
}