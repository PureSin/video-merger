package com.kelvinhanma.videomerger

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kelvinhanma.videomerger.model.Video
import com.kelvinhanma.videomerger.util.inflate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecyclerAdapter(private val context: Context, private val videos: List<Video>) :
    RecyclerView.Adapter<RecyclerAdapter.VideoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val inflatedView = parent.inflate(R.layout.video_list_row, false)
        return VideoHolder(inflatedView)
    }

    override fun getItemCount() = videos.size

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val video = videos[position]
        holder.bind(context, video)
    }

    class VideoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {
        val container: ViewGroup = v.findViewById(R.id.container)
        val name: TextView = v.findViewById(R.id.itemName)
        private val previewImage: ImageView = v.findViewById(R.id.itemImage)
        private val time: TextView = v.findViewById(R.id.itemDate)
        private val duration: TextView = v.findViewById(R.id.itemDuration)
        private val videoPlayer : VideoView = v.findViewById(R.id.videoPlayer)
        private lateinit var video : Video
        private lateinit var videoUri : Uri
        private var isPlaying = false

        fun bind(context: Context, video: Video) {
            this.video = video
            name.text = video.name

            // TODO figure out why duration/date aren't showing
            if (video.dateTaken > 0) {
                val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
                val netDate = Date(video.dateTaken)
                time.text = sdf.format(netDate)
            } else {
                time.visibility = View.INVISIBLE
            }

            if (video.duration > 0) {
                duration.text = TimeUnit.MICROSECONDS.toSeconds(video.duration.toLong()).toString()
            } else {
                duration.visibility = View.INVISIBLE
            }
            videoUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.id
            )
            videoPlayer.setVideoURI(videoUri)
            Glide.with(context).load(videoUri).into(previewImage)

            container.setOnClickListener(this)
            container.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.d("RecyclerAdapter", "Tap on $videoUri with status $isPlaying")
            isPlaying = !isPlaying

            previewImage.visibility = if (isPlaying) View.GONE else View.VISIBLE
            videoPlayer.visibility = if (isPlaying) View.VISIBLE else View.GONE

            if (isPlaying) {
                videoPlayer.start()
            } else {
                videoPlayer.stopPlayback()
            }
        }

        override fun onLongClick(v: View?): Boolean {
            Log.d("RecyclerAdapter", "Selected $videoUri")
            return true
        }
    }
}