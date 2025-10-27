package eu.kanade.tachiyomi.ui.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.notification.NotificationHandler
import eu.kanade.tachiyomi.data.notification.NotificationReceiver
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.util.system.notificationManager
import android.R as AR

/**
 * Class used to show BigPictureStyle notifications
 */
class SaveImageNotifier(private val context: Context) {

    /**
     * Notification builder.
     */
    private val notificationBuilder = NotificationCompat.Builder(context, Notifications.CHANNEL_COMMON)

    /**
     * Id of the notification.
     */
    private val notificationId: Int
        get() = Notifications.ID_DOWNLOAD_IMAGE

    /**
     * Called when image download/copy is complete. This method must be called in a background
     * thread.
     *
     * @param file image file containing downloaded page image.
     */
    fun onComplete(file: UniFile) {
        val request = ImageRequest.Builder(context).memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED)
            .data(file.uri)
            .size(720, 1280)
            .target(
                onSuccess = {
                    val bitmap = (it as BitmapDrawable).bitmap
                    if (bitmap != null) {
                        showCompleteNotification(file, bitmap)
                    } else {
                        onError(null)
                    }
                },
            ).build()
        context.imageLoader.enqueue(request)
    }

    private fun showCompleteNotification(file: UniFile, image: Bitmap) {
        with(notificationBuilder) {
            setContentTitle(context.getString(MR.strings.picture_saved))
            setSmallIcon(R.drawable.ic_photo_24dp)
            setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
            setLargeIcon(image)
            setAutoCancel(true)
            color = ContextCompat.getColor(context, R.color.secondaryTachiyomi)
            // Clear old actions if they exist
            clearActions()

            setContentIntent(NotificationHandler.openImagePendingActivity(context, file))
            // Share action
            addAction(
                R.drawable.ic_share_24dp,
                context.getString(MR.strings.share),
                NotificationReceiver.shareImagePendingBroadcast(context, file.filePath!!, notificationId),
            )
            // Delete action
            addAction(
                R.drawable.ic_delete_24dp,
                context.getString(MR.strings.delete),
                NotificationReceiver.deleteImagePendingBroadcast(context, file.filePath!!, notificationId),
            )

            updateNotification()
        }
    }

    /**
     * Clears the notification message.
     */
    fun onClear() {
        context.notificationManager.cancel(notificationId)
    }

    private fun updateNotification() {
        // Displays the progress bar on notification
        context.notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Called on error while downloading image.
     * @param error string containing error information.
     */
    fun onError(error: String?) {
        // Create notification
        with(notificationBuilder) {
            setContentTitle(context.getString(MR.strings.download_error))
            setContentText(error ?: context.getString(MR.strings.unknown_error))
            setSmallIcon(AR.drawable.ic_menu_report_image)
        }
        updateNotification()
    }
}
