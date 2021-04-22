package com.kelvinhanma.videomerger

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        holder.name.text = video.name

        if (video.dateTaken > 0) {
            val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
            val netDate = Date(video.dateTaken)
            holder.time.text = sdf.format(netDate)
        } else {
            holder.time.visibility = View.INVISIBLE
        }

        if (video.duration > 0) {
            holder.duration.text = TimeUnit.MICROSECONDS.toSeconds(video.duration.toLong()).toString()
        } else {
            holder.duration.visibility = View.INVISIBLE
        }
        val videoUri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.id
        )

        Glide.with(context).load(videoUri).into(holder.previewImage)
    }

    class VideoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var video: Video? = null
        val name: TextView
        val previewImage: ImageView
        val time: TextView
        val duration: TextView
        init {
            v.setOnClickListener(this)
            name = v.findViewById(R.id.itemName)
            previewImage = v.findViewById(R.id.itemImage)
            time = v.findViewById(R.id.itemDate)
            duration = v.findViewById(R.id.itemDuration)
        }

        override fun onClick(v: View?) {
            // TODO play video? or just select it
            Log.d("RecyclerAdapter", "clicked")
        }
    }
}