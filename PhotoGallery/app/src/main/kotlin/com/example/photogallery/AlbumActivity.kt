package com.example.photogallery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class AlbumActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        val bucketId = intent.getLongExtra("bucketId", -1L)
        val bucketName = intent.getStringExtra("bucketName") ?: ""

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = bucketName
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.photoRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        Thread {
            val photos = PhotoRepository.getPhotosInAlbum(this, bucketId)
            val uris = ArrayList(photos.map { it.uri.toString() })
            runOnUiThread {
                recyclerView.adapter = PhotoAdapter(photos) { position ->
                    val intent = Intent(this, ViewerActivity::class.java)
                    intent.putStringArrayListExtra("uris", uris)
                    intent.putExtra("startPosition", position)
                    startActivity(intent)
                }
            }
        }.start()
    }
}
