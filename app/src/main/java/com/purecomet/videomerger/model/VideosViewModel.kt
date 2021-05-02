package com.purecomet.videomerger.model

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.purecomet.videomerger.videoprocessing.VideoProcessor

class VideosViewModel(applicationContext: Application) : AndroidViewModel(applicationContext) {
    // TODO resolve context leak
    val context: Context = applicationContext
    var videosLiveData: MutableLiveData<List<Video>>
    // TODO this shouldn't be mutable but I also don't want to copy the list each update
    val selectedVideos: MutableLiveData<MutableList<Video>>

    init {
        Log.i("Model", "creating model")
        videosLiveData = MutableLiveData()
        selectedVideos = MutableLiveData()
        selectedVideos.value = ArrayList()
    }

    fun getVideosData(): LiveData<List<Video>> {
        return videosLiveData
    }

    fun getSelectedVideosData() : LiveData<MutableList<Video>> {
        return selectedVideos
    }

    fun loadData() {
        LoadDataTask(videosLiveData, context).execute()
    }

    fun mergeVideos() {
        MergeVideosTask(selectedVideos.value!!, context).execute()
    }

    fun addSelectedVideo(video: Video) {
        selectedVideos.value!!.add(video)
        selectedVideos.value = selectedVideos.value
    }

    fun removeSelectedVideo(video: Video) {
        selectedVideos.value!!.remove(video)
        selectedVideos.value = selectedVideos.value
    }

    private class LoadDataTask(
        videosLiveData: MutableLiveData<List<Video>>,
        context: Context
    ) : AsyncTask<Void, Void, List<Video>>() {
        val videosLiveData: MutableLiveData<List<Video>> = videosLiveData
        val context = context

        override fun doInBackground(vararg urls: Void): List<Video> {
            return VideoProcessor().run(context)
        }

        override fun onPostExecute(result: List<Video>) {
            videosLiveData.value = result
            Toast.makeText(context, "Found ${result.size} videos.", Toast.LENGTH_SHORT).show()
        }
    }

    private class MergeVideosTask(
        selectVideos: List<Video>,
        context: Context
    ) : AsyncTask<Void, Void, Video?>() {
        val selectVideos: List<Video> = selectVideos
        val context = context

        override fun doInBackground(vararg urls: Void) : Video? {
            return VideoProcessor().mergeSelectedVideos(context, selectVideos)
        }

        override fun onPostExecute(result: Video?) {
        }
    }
}