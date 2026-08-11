package com.example.photogallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var permissionLayout: View
    private lateinit var emptyText: TextView

    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    private val permissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadAlbums()
        } else {
            showPermissionRequest()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.albumRecyclerView)
        permissionLayout = findViewById(R.id.permissionLayout)
        emptyText = findViewById(R.id.emptyText)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        findViewById<Button>(R.id.grantButton).setOnClickListener {
            permissionLauncher.launch(readImagesPermission)
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasPermission()) {
            loadAlbums()
        } else {
            showPermissionRequest()
        }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, readImagesPermission) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionRequest() {
        permissionLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE
    }

    private fun loadAlbums() {
        permissionLayout.visibility = View.GONE
        Thread {
            val albums = PhotoRepository.getAlbums(this)
            runOnUiThread {
                if (albums.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = AlbumAdapter(albums) { album ->
                        val intent = Intent(this, AlbumActivity::class.java)
                        intent.putExtra("bucketId", album.id)
                        intent.putExtra("bucketName", album.name)
                        startActivity(intent)
                    }
                }
            }
        }.start()
    }
}
