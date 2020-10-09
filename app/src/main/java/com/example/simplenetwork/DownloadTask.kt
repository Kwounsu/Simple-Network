package com.example.simplenetwork

import android.net.ConnectivityManager
import android.os.AsyncTask
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Implementation of AsyncTask designed to fetch data from the network.
 */
private class DownloadTask(callback: DownloadCallback<String>)
    : AsyncTask<String, Int, DownloadTask.Result>() {

    private var callback: DownloadCallback<String>? = null

    init {
        setCallback(callback)
    }

    internal fun setCallback(callback: DownloadCallback<String>) {
        this.callback = callback
    }

    /**
     * Wrapper class that serves as a union of a result value and an exception. When the download
     * task has completed, either the result value or exception can be a non-null value.
     * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
     */
    internal class Result {
        var resultValue: String? = null
        var exception: Exception? = null

        constructor(resultValue: String) {
            this.resultValue = resultValue
        }

        constructor(exception: Exception) {
            this.exception = exception
        }
    }

    /**
     * Cancel background network operation if we do not have network connectivity.
     */
    override fun onPreExecute() {
        if (callback != null) {
            val networkInfo = callback?.getActiveNetworkInfo()
            if (networkInfo?.isConnected == false
                || networkInfo?.type != ConnectivityManager.TYPE_WIFI
                && networkInfo?.type != ConnectivityManager.TYPE_MOBILE) {
                // If no connectivity, cancel task and update Callback with null data.
                callback?.updateFromDownload(null)
                cancel(true)
            }
        }
    }

    /**
     * Defines work to perform on the background thread.
     */
    override fun doInBackground(vararg urls: String): DownloadTask.Result? {
        var result: Result? = null
        if (!isCancelled && urls.isNotEmpty()) {
            val urlString = urls[0]
            result = try {
                val url = URL(urlString)
                val resultString = downloadUrl(url)
                if (resultString != null) {
                    Result(resultString)
                } else {
                    throw IOException("No response received.")
                }
            } catch (e: Exception) {
                Result(e)
            }

        }
        return result
    }

    /**
     * Updates the DownloadCallback with the result.
     */
    override fun onPostExecute(result: Result?) {
        callback?.apply {
            result?.exception?.also { exception ->
                updateFromDownload(exception.message)
                return
            }
            result?.resultValue?.also { resultValue ->
                updateFromDownload(resultValue)
                return
            }
            finishDownloading()
        }
    }

    /**
     * Override to add special behavior for cancelled AsyncTask.
     */
    override fun onCancelled(result: Result) {}

    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    @Throws(IOException::class)
    private fun downloadUrl(url: URL): String? {
        var connection: HttpsURLConnection? = null
        return try {
            connection = (url.openConnection() as? HttpsURLConnection)
            connection?.run {
                // Timeout for reading InputStream arbitrarily set to 3000ms.
                readTimeout = 3000
                // Timeout for connection.connect() arbitrarily set to 3000ms.
                connectTimeout = 3000
                // For this use case, set HTTP method to GET.
                requestMethod = "GET"
                // Already true by default but setting just in case; needs to be true since this request
                // is carrying an input (response) body.
                doInput = true
                // Open communications link (network traffic occurs here).
                connect()
                publishProgress(CONNECT_SUCCESS)
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw IOException("HTTP error code: $responseCode")
                }
                // Retrieve the response body as an InputStream.
                publishProgress(GET_INPUT_STREAM_SUCCESS, 0)
                inputStream?.let { stream ->
                    // Converts Stream to String with max length of 500.
                    readStream(stream, 500)
                }
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            connection?.inputStream?.close()
            connection?.disconnect()
        }
    }
}