package fhnw.emoba.thatsapp.data

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import fhnw.emoba.R
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ImageService(val context: Context) {
    fun loadImage(url: String): ImageBitmap {
        try {
            val url = URL("$url")
            val conn = url.openConnection() as HttpsURLConnection
            conn.connect()

            val inputStream = conn.inputStream
            val allBytes = inputStream.readBytes()
            inputStream.close()

            val bitmap = BitmapFactory.decodeByteArray(allBytes, 0, allBytes.size)

            return bitmap.asImageBitmap()
        }
        catch (e: Exception) {
            Log.d("ERROR", e.stackTraceToString())
            return BitmapFactory.decodeResource(context.resources, R.drawable.no_image).asImageBitmap()
        }
    }

    fun getDefaultImage(): ImageBitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.no_image).asImageBitmap()
    }
}