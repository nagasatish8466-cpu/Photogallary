package com.example.photogallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ViewerAdapter(
    private val uris: List<android.net.Uri>
) : RecyclerView.Adapter<ViewerAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fullImage: ZoomableImageView = view.findViewById(R.id.fullImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_viewer_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val uri = uris[position]
        val target = 1600
        holder.fullImage.setImageBitmap(null)
        ThumbnailLoaderFull.load(holder.itemView.context, uri, holder.fullImage, target)
    }

    override fun getItemCount() = uris.size
}

/**
 * Loads a larger, full-resolution-ish bitmap for the viewer pages on a background thread.
 */
object ThumbnailLoaderFull {
    fun load(context: android.content.Context, uri: android.net.Uri, imageView: ZoomableImageView, reqSize: Int) {
        imageView.tag = uri
        val appContext = context.applicationContext
        Thread {
            val bitmap = ThumbnailLoader.decodeSampledBitmap(appContext, uri, reqSize, reqSize)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (imageView.tag == uri && bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }.start()
    }
}
