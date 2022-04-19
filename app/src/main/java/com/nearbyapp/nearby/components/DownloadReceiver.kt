package com.nearbyapp.nearby.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class DownloadReceiver: BroadcastReceiver() {

    var handler: Handler? = null

    abstract class Handler(val ref: DownloadManagerHelper.DownloadRef) {
        abstract fun onDownloadCompleted()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String? = intent?.action
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
            val downloadId: Long = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            handler?.let {
                if (it.ref.id == downloadId) {
                    it.onDownloadCompleted()
                }
            }
        }
    }

    fun submitHandler(handler: Handler) {
        this.handler = handler
    }

}