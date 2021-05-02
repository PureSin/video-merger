package com.purecomet.videomerger.videoprocessing

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.purecomet.videomerger.model.Video
import java.io.*
import java.util.logging.Logger


/**
 * Holds code to handle videos
 */
class VideoProcessor {
    companion object {
        val LOGGER = Logger.getLogger("VideoProcessor")
    }

    private val projects: Array<String> = arrayOf(
        MediaStore.Video.VideoColumns._ID,
        MediaStore.Video.VideoColumns.DISPLAY_NAME,
        MediaStore.Video.VideoColumns.DATE_TAKEN,
        MediaStore.Video.VideoColumns.DURATION,
        MediaStore.Video.VideoColumns.DATA
    )

    private val VIDEOS_URI: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    fun run(context: Context): List<Video> {
        LOGGER.info("staring media scan.")

        val videos: MutableList<Video> = ArrayList()

        context.contentResolver.query(
            VIDEOS_URI,
            projects,
            null,
            null,
            MediaStore.Video.VideoColumns.DATE_TAKEN + " ASC LIMIT 20"
        )?.use { cursor ->
            LOGGER.info("Result: " + cursor.count)
            while (cursor.moveToNext()) {
                // Use an ID column from the projection to get
                // a URI representing the media item itself.
                val video = createVideoFromCursor(cursor)
                LOGGER.info(video.toString())
                videos.add(video)
            }
        }
        return videos
    }

    private fun createVideoFromCursor(cursor: Cursor): Video {
        val id =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
        val name =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME))
        val timestamp =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_TAKEN))
        val duration =
            cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))
        // TODO use contentprovider and fd instead?
        val videoUri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
        )
        val filePath =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA))
        return Video(id, name, timestamp, duration, videoUri, filePath)
    }

    fun mergeSelectedVideos(context: Context, videos: List<Video>): Video? {
        LOGGER.info("Merging Videos: $videos")
        val packageDir = context.filesDir
        val inputFile = "input_list.txt"
        val outputVideo = "out.mp4"

        // generate input list first
        val fileout: FileOutputStream = context.openFileOutput(inputFile, MODE_PRIVATE)
        val outputWriter = OutputStreamWriter(fileout)
        val sb = StringBuilder()
        videos.forEach {
            sb.appendln(
                "file \'${it.filePath}\'"
            )
        }
        outputWriter.write(sb.toString())
        outputWriter.close()

        when (val rc = FFmpeg.execute("-y -f concat -safe -0 -i \"$packageDir/$inputFile\" -c copy $packageDir/$outputVideo")) {
            RETURN_CODE_SUCCESS -> {
                LOGGER.info("Command execution completed successfully.")
            }
            RETURN_CODE_CANCEL -> {
                LOGGER.info("Command execution cancelled by user.")
                return null
            }
            else -> {
                LOGGER.info("Command execution failed with rc=%d and the output below. $rc")
                Config.printLastCommandOutput(Log.INFO)
                return null
            }
        }

        val openFileInput = context.openFileInput(outputVideo)
        // Insert into MediaStore
        val values = ContentValues()
        values.put(MediaStore.Video.Media.TITLE, "Merged Video")
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
        val contentResolver = context.contentResolver
        val uri: Uri =
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: return null

        LOGGER.info("MediaStore uri for insert: $uri")
        // Write to the uri
        uri.let {
            copy(openFileInput, contentResolver.openOutputStream(it)!!)

            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)
        }

        // Delete files
        // TODO handle failure paths to also delete
        context.deleteFile(inputFile)
        context.deleteFile(outputVideo)

        // query to get data for video
        context.contentResolver.query(
            uri,
            projects,
            null,
            null,
            null
        )?.use { cursor ->
            LOGGER.info("Result: " + cursor.count)
            while (cursor.moveToNext()) {
                // Use an ID column from the projection to get
                // a URI representing the media item itself.
                val video = createVideoFromCursor(cursor)
                LOGGER.info("Created $video")
                return video;
            }
        }

        return null
    }

    private fun copy(src: InputStream, out: OutputStream) {
        try {
            try {
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (src.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            } finally {
                out.close()
            }
        } finally {
            src.close()
        }
    }

    // TODO write tests
    fun detectMergeableVideos(videos: List<Video>, allowedDeltaSecs: Int = 5): List<List<Video>> {
        val result = ArrayList<ArrayList<Video>>()
        val size = videos.size

        for ((startIndex, video) in videos.withIndex()) {
            val maxTime = video.dateTaken + video.dateTaken
            var candidatesFound = false
            for (candidateVideoIndex in startIndex + 1 until size) {
                val candidateVideo = videos[candidateVideoIndex]
                if (candidateVideo.dateTaken in maxTime - allowedDeltaSecs..maxTime + allowedDeltaSecs) {
                    if (!candidatesFound) {
                        candidatesFound = true
                        val newList = ArrayList<Video>()
                        newList.add(video)
                        newList.add(candidateVideo)
                        result.add(newList)
                    } else {
                        result.last().add(candidateVideo)
                    }
                }
            }
        }
        return result
    }
}