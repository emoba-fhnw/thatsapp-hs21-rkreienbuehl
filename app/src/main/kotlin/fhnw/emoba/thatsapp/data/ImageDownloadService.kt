package fhnw.emoba.thatsapp.data

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import fhnw.emoba.R
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ImageDownloadService(val context: Context) {
    fun loadImage(
        url: String,
        onSuccess: (ImageBitmap) -> Unit,
        onError: (ImageBitmap) -> Unit
    ) {
        try {
            val url = URL("$url")
            val conn = url.openConnection() as HttpsURLConnection
            conn.setRequestProperty("User-Agent", "emoba_ThatsApp")
            conn.connect()

            val inputStream = conn.inputStream
            val allBytes = inputStream.readBytes()
            inputStream.close()

            val bitmap = BitmapFactory.decodeByteArray(allBytes, 0, allBytes.size).asImageBitmap()

            onSuccess.invoke(bitmap)
        }
        catch (e: Exception) {
            Log.d("ERROR", e.stackTraceToString())
            val bitmap =  BitmapFactory.decodeResource(context.resources, R.drawable.no_image).asImageBitmap()

            onError.invoke(bitmap)
        }
    }

    fun getDefaultImage(): ImageBitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.no_image).asImageBitmap()
    }
}