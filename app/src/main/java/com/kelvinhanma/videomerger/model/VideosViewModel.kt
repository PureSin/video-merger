package com.kelvinhanma.videomerger.model

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kelvinhanma.videomerger.videoprocessing.VideoProcessor

class VideosViewModel(applicationContext: Application) : AndroidViewModel(applicationContext) {
    var videosLiveData: MutableLiveData<List<Video>>
    val context: Context = applicationContext

    init {
        Log.i("Model", "creating model")
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