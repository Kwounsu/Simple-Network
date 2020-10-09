package com.example.simplenetwork

import android.content.Context
import android.os.AsyncTask.execute
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/*
* Since the NetworkFragment runs on the UI thread by default,
* it uses an AsyncTask to run the network operations on a background thread.
* This Fragment is considered headless because it doesn't reference any UI elements.
* Instead, it is only used to encapsulate logic and handle lifecycle events,
* leaving the host Activity to update the UI.
* */

private const val TAG = "NetworkFragment"
private const val URL_KEY = "UrlKey"

class NetworkFragment : Fragment() {
    private var callback: DownloadCallback<String>? = null
    private var downloadTask: DownloadTask? = null
    private var urlString: String? = null

    companion object {
        /**
         * Static initializer for NetworkFragment that sets the URL of the host it will be
         * downloading from.
         */
        fun getInstance(fragmentManager: FragmentManager, url: String): NetworkFragment {
            val networkFragment = NetworkFragment()
            val args = Bundle()
            args.putString(URL_KEY, url)
            networkFragment.arguments = args
            fragmentManager.beginTransaction().add(networkFragment, TAG).commit()
            return networkFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        urlString = arguments?.getString(URL_KEY)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // Host Activity will handle callbacks from task.
        callback = context as? DownloadCallback<String>
    }

    override fun onDetach() {
        super.onDetach()
        // Clear reference to host Activity to avoid memory leak.
        callback = null
    }

    override fun onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelDownload()
        super.onDestroy()
    }

    /**
     * Start non-blocking execution of DownloadTask.
     */
    fun startDownload() {
        cancelDownload()
        callback?.also {
            downloadTask = DownloadTask(it).apply {
                execute(urlString)
            }
        }
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    fun cancelDownload() {
        downloadTask?.cancel(true)
    }
}