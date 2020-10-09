package com.example.simplenetwork

import android.net.ConnectivityManager
import android.os.AsyncTask
import java.io.IOException
import java.net.URL

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
}