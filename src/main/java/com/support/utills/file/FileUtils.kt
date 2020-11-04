package com.support.utills.file

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import okio.Okio
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset

fun loadJSONFromAsset(activity: AppCompatActivity, fileName: String): String? {
    var json: String? = null
    try {
        val `is` = activity.assets.open(fileName)
        val size = `is`.available()
        val buffer = ByteArray(size)
        `is`.read(buffer)
        `is`.close()
        json = String(buffer, Charset.forName("UTF-8"))
    } catch (ex: IOException) {
        ex.printStackTrace()
        return null
    }

    return json
}

fun File.CopyFile(targetFile: File): File {
    val start = System.currentTimeMillis()
    targetFile.delete()
    Okio.buffer(Okio.sink(targetFile)).use { sink ->
        Okio.source(this).use { bufferSource ->
            sink.writeAll(bufferSource)
            println("okio: " + (System.currentTimeMillis() - start) + "ms")
        }
    }
    return targetFile
}

fun File.renameFile(_FileName: String): File? {
    val destFile = File(this.parent + "/" + _FileName)
    this.renameTo(destFile)
    return destFile
}

fun File.getVideoWidthHeight(): MutableList<Int> {
    val heightWidth = mutableListOf(0, 0)
    var retriever: MediaMetadataRetriever? = null
    var bmp: Bitmap? = null
    var inputStream: FileInputStream? = null
    val mWidthHeight = 0
    try {
        retriever = MediaMetadataRetriever()
        inputStream = FileInputStream(this.absolutePath)
        retriever.setDataSource(inputStream.fd)
        bmp = retriever.frameAtTime
        heightWidth[0] = bmp.width
        heightWidth[1] = bmp.height
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever?.release()
        inputStream?.close()
    }
    System.gc()
    return heightWidth;
}