package com.example.simplenetwork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity(), DownloadCallback<String> {

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private var networkFragment: NetworkFragment? = null

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private var downloading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkFragment = NetworkFragment.getInstance(supportFragmentManager, "https://www.google.com")

        private fun startDownload() {
            if (!downloading) {
                // Execute the async download.
                networkFragment?.apply {
                    startDownload()
                    downloading = true
                }
            }
        }
    }

    override fun updateFromDownload(result: String?) {
        // Update your UI here based on result of download.
    }

    override fun getActiveNetworkInfo(): NetworkInfo {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    override fun onProgressUpdate(progressCode: Int, percentComplete: Int) {
        when (progressCode) {
            // You can add UI behavior for progress updates here.
            ERROR -> {
            }
            CONNECT_SUCCESS -> {
            }
            GET_INPUT_STREAM_SUCCESS -> {
            }
            PROCESS_INPUT_STREAM_IN_PROGRESS -> {
            }
            PROCESS_INPUT_STREAM_SUCCESS -> {
            }
        }
    }

    override fun finishDownloading() {
        downloading = false
        networkFragment?.cancelDownload()
    }
}