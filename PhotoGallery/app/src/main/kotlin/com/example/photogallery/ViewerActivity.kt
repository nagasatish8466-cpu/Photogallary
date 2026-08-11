package com.example.photogallery

import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.text.format.Formatter
import android.view.MenuItem
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import java.util.Date

class ViewerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var uris: MutableList<Uri>
    private var pendingDeleteUri: Uri? = null

    private val deleteRequestLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            pendingDeleteUri?.let { removeFromListAndRefresh(it) }
        }
        pendingDeleteUri = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)

        val uriStrings = intent.getStringArrayListExtra("uris") ?: arrayListOf()
        uris = uriStrings.map { Uri.parse(it) }.toMutableList()
        val startPosition = intent.getIntExtra("startPosition", 0)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewerAdapter(uris)
        viewPager.setCurrentItem(startPosition, false)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.viewer_menu)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener { item -> onMenuItemClick(item) }
    }

    private fun currentUri(): Uri? {
        if (uris.isEmpty()) return null
        val pos = viewPager.currentItem
        if (pos < 0 || pos >= uris.size) return null
        return uris[pos]
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        val uri = currentUri() ?: return false
        when (item.itemId) {
            R.id.action_share -> shareImage(uri)
            R.id.action_delete -> confirmDelete(uri)
            R.id.action_info -> showInfo(uri)
        }
        return true
    }

    private fun shareImage(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share photo"))
    }

    private fun confirmDelete(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> deleteImage(uri) }
            .show()
    }

    private fun deleteImage(uri: Uri) {
        try {
            contentResolver.delete(uri, null, null)
            removeFromListAndRefresh(uri)
        } catch (securityException: SecurityException) {
            val intentSender: IntentSender? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && securityException is RecoverableSecurityException) {
                    securityException.userAction.actionIntent.intentSender
                } else {
                    null
                }
            if (intentSender != null) {
                pendingDeleteUri = uri
                deleteRequestLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
        }
    }

    private fun removeFromListAndRefresh(uri: Uri) {
        val index = uris.indexOf(uri)
        if (index == -1) return
        uris.removeAt(index)
        if (uris.isEmpty()) {
            finish()
            return
        }
        viewPager.adapter = ViewerAdapter(uris)
        val newPos = index.coerceAtMost(uris.size - 1)
        viewPager.setCurrentItem(newPos, false)
    }

    private fun showInfo(uri: Uri) {
        var name = ""
        var size = 0L
        var width = 0
        var height = 0
        var dateAdded = 0L

        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_ADDED
        )
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)) ?: ""
                size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))
                height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))
                dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
            }
        }

        val dateStr = DateFormat.format("MMM dd, yyyy HH:mm", Date(dateAdded * 1000)).toString()
        val sizeStr = Formatter.formatShortFileSize(this, size)

        val message = "Name: $name\nDate: $dateStr\nSize: $sizeStr\nDimensions: ${width}x$height"

        AlertDialog.Builder(this)
            .setTitle("Photo info")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
