package com.kelvinhanma.videomerger.model

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kelvinhanma.videomerger.videoprocessing.VideoProcessor

class VideosViewModel(val applicationContext: Context) : ViewModel() {
    var videosLiveData: MutableLiveData<List<Video>>
    val context: Context

    init {
        this.context = applicationContext
        videosLiveData = MutableLiveData()
    }

    fun getData(): LiveData<List<Video>> {
        return videosLiveData
    }

    fun loadData() {
        LoadDataTask(videosLiveData, context).execute();
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
        }
    }
}