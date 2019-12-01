package com.kelvinhanma.videomerger.videoprocessing

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.kelvinhanma.videomerger.util.toHumanReadableString
import java.util.logging.Logger

/**
 * Holds code to handle videos
 */
class VideoProcessor {
    companion object {
        val LOGGER = Logger.getLogger("VideoProcessor")
    }

    fun run(context: Context) {
        LOGGER.info("staring media scan.")

        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection =
            arrayOf(
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.DURATION
            )
        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.Video.VideoColumns.DATE_TAKEN + " ASC"
        )?.use { cursor ->
            LOGGER.info("Result: " + cursor.count)
            while (cursor.moveToNext()) {
                // Use an ID column from the projection to get
                // a URI representing the media item itself.
                val timestamp =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_TAKEN))
                LOGGER.info(
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME)) + " : " + timestamp.toHumanReadableString()
                            + " : " + cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))
                )
            }
        }
    }
}