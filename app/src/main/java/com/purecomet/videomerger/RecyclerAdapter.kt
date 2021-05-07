package com.purecomet.videomerger

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.purecomet.videomerger.model.Video
import com.purecomet.videomerger.model.VideosViewModel
import com.purecomet.videomerger.util.inflate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecyclerAdapter(private val context: Context, private val viewModel: VideosViewModel) :
    RecyclerView.Adapter<RecyclerAdapter.VideoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val inflatedView = parent.inflate(R.layout.video_list_row, false)
        return VideoHolder(inflatedView)
    }

    override fun getItemCount() = viewModel.videosLiveData.value!!.size

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val video = viewModel.videosLiveData.value!![position]
        holder.bind(context, video, viewModel)
    }

    class VideoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener,
        View.OnLongClickListener {
        private val container: ViewGroup = v.findViewById(R.id.container)
        private lateinit var model: VideosViewModel
        val name: TextView = v.findViewById(R.id.itemName)
        private val previewImage: ImageView = v.findViewById(R.id.itemImage)
        private val time: TextView = v.findViewById(R.id.itemDate)
        private val duration: TextView = v.findViewById(R.id.itemDuration)
        private val videoPlayer: VideoView = v.findViewById(R.id.videoPlayer)
        private lateinit var video: Video
        private var isPlaying = false
        private var isSelected = false

        fun bind(context: Context, video: Video, model: VideosViewModel) {
            this.model = model
            this.video = video
            name.text = context.resources.getString(R.string.video_title, video.name)

            if (video.dateTaken > 0) {
                val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
                val netDate = Date(video.dateTaken)
                time.text = context.resources.getString(R.string.video_date, sdf.format(netDate))
            } else {
                time.visibility = View.GONE
            }

            if (video.duration > 0) {
                duration.text = context.resources.getString(
                    R.string.video_duration,
                    TimeUnit.MILLISECONDS.toSeconds(video.duration.toLong())
                )
            } else {
                duration.visibility = View.GONE
            }
            videoPlayer.setVideoURI(video.uri)
            Glide.with(context).load(video.uri).into(previewImage)

            container.setOnClickListener(this)
            container.setOnLongClickListener(this)
            isSelected = model.selectedVideos.value!!.contains(video)
            container.setBackgroundColor(if (isSelected) Color.BLUE else Color.WHITE)
        }

        override fun onClick(v: View?) {
            Log.d("RecyclerAdapter", "Tap on ${video.uri} with status $isPlaying")
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
            Log.d("RecyclerAdapter", "Selected ${video.uri}")
            isSelected = !isSelected
            // TODO pick better colors
            if (isSelected) {
                model.addSelectedVideo(video)
            } else {
                model.removeSelectedVideo(video)
            }
            isSelected = model.selectedVideos.value!!.contains(video)
            container.setBackgroundColor(if (isSelected) Color.BLUE else Color.WHITE)
            return true
        }
    }
}