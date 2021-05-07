package com.purecomet.videomerger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.purecomet.videomerger.model.Video
import com.purecomet.videomerger.model.VideosViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var model: VideosViewModel
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapter
    private lateinit var swipeContainer: SwipeRefreshLayout

    companion object {
        const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // request permission
        if (!checkPermissions()) {
            return;
        }

        initUi()
    }

    private fun initUi() {
        val selectedVideosTextView = findViewById<TextView>(R.id.selectedCount)
        model = ViewModelProviders.of(this).get(VideosViewModel::class.java)
        // Create the observer which updates the UI.
        val videosObserver: Observer<List<Video>> = Observer { // Update the UI
            adapter = RecyclerAdapter(baseContext, model)
            recyclerView.adapter = adapter

            swipeContainer.isRefreshing = false
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        model.getVideosData().observe(this, videosObserver)

        val selectedObserver: Observer<MutableList<Video>> = Observer {
            selectedVideosTextView.text = resources.getQuantityString(
                R.plurals.select_videos,
                model.selectedVideos.value!!.size,
                model.selectedVideos.value!!.size
            )
        }
        model.getSelectedVideosData().observe(this, selectedObserver)

        swipeContainer = findViewById(R.id.swipeContainer)
        swipeContainer.setOnRefreshListener {
            model.loadData(baseContext)
        }

        val mergeButton = findViewById<Button>(R.id.mergeButton)
        mergeButton.setOnClickListener { model.mergeVideos(baseContext) }

        recyclerView = findViewById(R.id.videos_list)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        if (model.videosLiveData.value != null) {
            adapter = RecyclerAdapter(baseContext, model)
            recyclerView.adapter = adapter
        }
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        model.loadData(baseContext)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST -> {
                initUi()
                return
            }
        }
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST
                )
                return false;
            }
            return true;
        }
        return true;
    }
}
