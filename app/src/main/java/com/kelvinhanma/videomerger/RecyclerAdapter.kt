package com.kelvinhanma.videomerger

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelvinhanma.videomerger.model.Video
import com.kelvinhanma.videomerger.util.inflate

class RecyclerAdapter(private val videos: List<Video>) : RecyclerView.Adapter<RecyclerAdapter.VideoHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val inflatedView = parent.inflate(R.layout.video_list_row, false)
        return VideoHolder(inflatedView)
    }

    override fun getItemCount() = videos.size

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val video = videos[position]
        holder.name.text = video.name
    }

    class VideoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var video: Video? = null
        val name: TextView

        init {
            v.setOnClickListener(this)
            name = v.findViewById(R.id.itemName)
        }

        override fun onClick(v: View?) {
            Log.d("RecyclerAdapter", "clicked")
        }
    }
}