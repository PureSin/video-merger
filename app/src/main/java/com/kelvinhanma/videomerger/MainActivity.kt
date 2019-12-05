package com.kelvinhanma.videomerger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kelvinhanma.videomerger.model.VideosViewModel

class MainActivity : AppCompatActivity() {
    lateinit var model: VideosViewModel

    companion object {
        val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // request permission
        if (!checkPermissions()) {
            return;
        }

        initUi()

        model = VideosViewModel(applicationContext)
    }

    // TODO add a view to list detected video
    private fun initUi() {
        val scanButton = findViewById<Button>(R.id.scan_button)
        scanButton.setOnClickListener {
            model.loadData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
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
