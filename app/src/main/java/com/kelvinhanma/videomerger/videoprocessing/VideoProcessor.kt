package com.kelvinhanma.videomerger.videoprocessing

import android.content.ContentUris
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.kelvinhanma.videomerger.model.Video
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.logging.Logger


/**
 * Holds code to handle videos
 */
class VideoProcessor {
    companion object {
        val LOGGER = Logger.getLogger("VideoProcessor")
    }

    fun run(context: Context): List<Video> {
        LOGGER.info("staring media scan.")
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection =
            arrayOf(
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.DATA
            )

        val videos: MutableList<Video> = ArrayList()

        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.Video.VideoColumns.DATE_TAKEN + " ASC LIMIT 20"
        )?.use { cursor ->
            LOGGER.info("Result: " + cursor.count)
            while (cursor.moveToNext()) {
                // Use an ID column from the projection to get
                // a URI representing the media item itself.
                val id =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME))
                val timestamp =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_TAKEN))
                val duration =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))
                // TODO use contentprovider and fd instead?
                val filePath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA))
                val video = Video(id, name, timestamp, duration, filePath)
                LOGGER.info(video.toString())
                videos.add(video)
            }
        }
        return videos
    }

    fun mergeSelectedVideos(context: Context, videos: List<Video>): Video {
        LOGGER.info("Merging Videos: $videos")
        val packageDir = context.applicationInfo.dataDir
        val inputFile = "input_list.txt"
        val outputVideo = "$packageDir/out.mp4"

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

        // ffmpeg -f concat -safe 0 -i mylist.txt -c copy output.wav
        when (val rc = FFmpeg.execute("-y -f concat -safe -0 -i \"$packageDir/files/$inputFile\" -c copy $outputVideo")) {
            RETURN_CODE_SUCCESS -> {
                LOGGER.info("Command execution completed successfully.")
            }
            RETURN_CODE_CANCEL -> {
                LOGGER.info("Command execution cancelled by user.")
            }
            else -> {
                LOGGER.info("Command execution failed with rc=%d and the output below. $rc")
                Config.printLastCommandOutput(Log.INFO)
            }
        }

        return Video(1, "Test", 2, 3, "test")
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