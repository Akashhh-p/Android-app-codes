package com.example.intentdemonavigation.worker

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val imageUrl = "https://www.gstatic.com/webp/gallery/1.sm.webp"
        val fileName = "background_download.webp"
        var uri: Uri? = null

        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Server returned HTTP ${connection.responseCode}")
            }

            val contentResolver = applicationContext.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/webp")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                uri = contentResolver.insert(collection, contentValues) ?: throw Exception("Failed to create MediaStore entry")

                val inputStream: InputStream = connection.inputStream
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    inputStream.use { input ->
                        input.copyTo(outputStream)
                    }
                } ?: throw Exception("Failed to open output stream")

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)

            } else {
                // Fallback for older Android versions
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                
                val inputStream: InputStream = connection.inputStream
                FileOutputStream(file).use { output ->
                    inputStream.use { input ->
                        input.copyTo(output)
                    }
                }
                uri = Uri.fromFile(file)
                
                // For older versions, we should still tell the MediaStore about the new file so it shows in File Manager
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/webp")
                }
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }

            // Verify the file through ContentResolver
            var fileSize: Long = 0
            contentResolver.query(uri!!, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    fileSize = cursor.getLong(sizeIndex)
                    val name = cursor.getString(nameIndex)
                    Log.d("DownloadWorker", "Verified File: $name, Size: $fileSize bytes")
                }
            }

            if (fileSize > 0) {
                Log.d("DownloadWorker", "Download successful: $uri")
                return Result.success(workDataOf("uri" to uri.toString()))
            } else {
                throw Exception("File verification failed: Size is 0")
            }

        } catch (e: Exception) {
            Log.e("DownloadWorker", "Download failed", e)
            uri?.let {
                applicationContext.contentResolver.delete(it, null, null)
            }
            return Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))
        }
    }
}