package com.kelvinhanma.videomerger

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.kelvinhanma.videomerger.videoprocessing.VideoProcessor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO add a view to list detected video
        val scanButton = findViewById<Button>(R.id.scan_button)
        scanButton.setOnClickListener {
            VideoProcessor().run()
        }
    }
}
