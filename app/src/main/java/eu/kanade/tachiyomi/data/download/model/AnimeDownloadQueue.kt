package eu.kanade.tachiyomi.data.download.model

import androidx.annotation.CallSuper
import eu.kanade.tachiyomi.util.system.launchUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

sealed class AnimeDownloadQueue {
    interface Listener {
        val progressJobs: MutableMap<AnimeDownload, Job>

        // Override with presenterScope or viewScope
        val queueListenerScope: CoroutineScope

        fun onProgressUpdate(download: AnimeDownload)
        fun onQueueUpdate(download: AnimeDownload)

        // Subscribe on presenter/controller creation on UI thread
        @CallSuper
        fun onStatusChange(download: AnimeDownload) {
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
        private fun launchProgressJob(download: AnimeDownload) {
            val job = queueListenerScope.launchUI {
                download.progressFlow.collectLatest {
                    onProgressUpdate(download)
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
        private fun cancelProgressJob(download: AnimeDownload) {
            progressJobs.remove(download)?.cancel()
        }
    }
}
