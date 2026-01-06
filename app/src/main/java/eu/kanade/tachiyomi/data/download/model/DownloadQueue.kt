package eu.kanade.tachiyomi.data.download.model

import androidx.annotation.CallSuper
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.util.system.launchUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

sealed class DownloadQueue {
    interface Listener {
        val progressJobs: MutableMap<Download, Job>

        // Override with presenterScope or viewScope
        val queueListenerScope: CoroutineScope

        fun onPageProgressUpdate(download: Download) {
            onProgressUpdate(download)
        }
        fun onProgressUpdate(download: Download)
        fun onQueueUpdate(download: Download)

        // Subscribe on presenter/controller creation on UI thread
        @CallSuper
        fun onStatusChange(download: Download) {
            when (download.status) {
                Download.State.DOWNLOADING -> {
                    launchProgressJob(download)
                    // Initial update of the downloaded pages
                    onQueueUpdate(download)
                }
                Download.State.DOWNLOADED -> {
                    cancelProgressJob(download)

                    onProgressUpdate(download)
                    onQueueUpdate(download)
                }
                Download.State.ERROR -> cancelProgressJob(download)
                else -> {
                    /* unused */
                }
            }
        }

        /**
         * Observe the progress of a download and notify the view.
         *
         * @param download the download to observe its progress.
         */
        private fun launchProgressJob(download: Download) {
            val job = queueListenerScope.launchUI {
                while (download.pages == null) {
                    delay(50)
                }

                val progressFlows = download.pages!!.map(Page::progressFlow)
                combine(progressFlows, Array<Int>::sum)
                    .distinctUntilChanged()
                    .debounce(50)
                    .collectLatest {
                        onPageProgressUpdate(download)
                    }
            }

            // Avoid leaking jobs
            progressJobs.remove(download)?.cancel()

            progressJobs[download] = job
        }

        /**
         * Unsubscribes the given download from the progress subscriptions.
         *
         * @param download the download to unsubscribe.
         */
        private fun cancelProgressJob(download: Download) {
            progressJobs.remove(download)?.cancel()
        }
    }
}
