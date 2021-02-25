package com.support.utills.file

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.constraint.BuildConfig.APPLICATION_ID
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.support.BuildConfig
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.Util
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.*
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


const val MIME_TYPE_AUDIO = "audio/*"
const val MIME_TYPE_TEXT = "text/*"
const val MIME_TYPE_IMAGE = "image/*"
const val MIME_TYPE_VIDEO = "video/*"
const val MIME_TYPE_APP = "application/*"
const val HIDDEN_PREFIX = "."

private const val AUTHORITY: String = "$APPLICATION_ID.provider"
private val format = DecimalFormat("#.##")
private const val MiB = (1024 * 1024).toLong()
private const val KiB: Long = 1024
private const val TAG = "FileUtils"

@Throws(IOException::class)
fun Context.readJsonAsset(fileName: String): String {
    val inputStream = assets.open(fileName)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()
    return String(buffer, Charsets.UTF_8)
}

fun loadJSONFromAsset(context: Context, fileName: String): String? {
    var json: String? = null
    try {
        val `is` = context.assets.open(fileName)
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
        heightWidth[0] = bmp?.width!!
        heightWidth[1] = bmp?.height!!
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever?.release()
        inputStream?.close()
    }
    System.gc()
    return heightWidth;
}

fun Context.scanMediaPath(file: File) {
    sendBroadcast(
            Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + file.absolutePath)
            )
    )
}

fun getUriForDrawable(mActivity: Activity, localCurrentImage: Int): Uri {
    return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(mActivity.resources.getResourcePackageName(localCurrentImage))
            .appendPath(mActivity.resources.getResourceTypeName(localCurrentImage))
            .appendPath(mActivity.resources.getResourceEntryName(localCurrentImage))
            .build()
}

fun copyAssets(context: Context, assetFolderName: String, targetPath: String?) {
    val assetManager = context.assets
    var files: Array<String>? = null
    try {
        files = assetManager.list(assetFolderName)
    } catch (e: IOException) {
        Log.e("tag", "Failed to get asset file list.", e)
    }
    if (files != null) for (filename in files) {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = assetManager.open(assetFolderName + File.separator + filename)
            val outFile = File(targetPath, filename)
            out = FileOutputStream(outFile)
            copy(`in`, out)
            return
        } catch (e: IOException) {
            Log.e("tag", "Failed to copy asset file: $filename", e)
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close()
                } catch (e: IOException) {
                    // NOOP
                }
            }
        }
    }
}

private fun copy(input: InputStream, output: OutputStream): Long {
    var count: Long = 0
    var n: Int
    val buffer = ByteArray(1024)
    while (-1 != input.read(buffer).also { n = it }) {
        output.write(buffer, 0, n)
        count += n.toLong()
    }
    return count
}

fun unzip(zipFile: File?, targetDirectory: File?) {
    val zis = ZipInputStream(
            BufferedInputStream(FileInputStream(zipFile)))
    try {
        var ze: ZipEntry
        var count: Int
        val buffer = ByteArray(8192)
        while (zis.nextEntry.also { ze = it } != null) {
            val file = File(targetDirectory, ze.name)
            val dir = if (ze.isDirectory) file else file.parentFile
            if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException("Failed to ensure directory: " +
                    dir.absolutePath)
            if (ze.isDirectory) continue
            val fout = FileOutputStream(file)
            try {
                while (zis.read(buffer).also { count = it } != -1) fout.write(buffer, 0, count)
            } finally {
                fout.close()
            }
            /* if time should be restored as well
        long time = ze.getTime();
        if (time > 0)
            file.setLastModified(time);
        */
        }
    } finally {
        zis.close()
    }
}

fun getRawUri(context: Context, filename: String): Uri? {
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + context.packageName + "/raw/" + filename)
}

fun getDrawableUri(context: Context, filename: String): Uri? {
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + context.packageName + "/drawable/" + filename)
}

fun getMipmapUri(context: Context, filename: String): Uri? {
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + context.packageName + "/mipmap/" + filename)
}

fun getAssetUri(fileName: String): Uri? {
    return Uri.parse("asset:///$fileName")
}

fun copyFileOrDirectory(srcDir: String?, dstDir: String?) {
    try {
        val src = File(srcDir)
        val dst = File(dstDir, src.name)
        if (src.isDirectory) {
            val files = src.list()
            val filesLength = files.size
            for (i in 0 until filesLength) {
                val src1 = File(src, files[i]).path
                val dst1 = dst.path
                copyFileOrDirectory(src1, dst1)
            }
        } else {
            copyFile(src, dst)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun copyFile(sourceFile: File?, destFile: File) {
    if (!destFile.parentFile.exists()) destFile.parentFile.mkdirs()
    if (!destFile.exists()) {
        destFile.createNewFile()
    }
    var source: FileChannel? = null
    var destination: FileChannel? = null
    try {
        source = FileInputStream(sourceFile).channel
        destination = FileOutputStream(destFile).channel
        destination.transferFrom(source, 0, source.size())
    } finally {
        source?.close()
        destination?.close()
    }
}

fun mStoreImage(context: Context, mBitmap: Bitmap) {
    try {
        val cachePath: File = File(context.getCacheDir(), "images")
        cachePath.mkdirs() // don't forget to make the directory
        val stream = FileOutputStream("$cachePath/image.png") // overwrites this image every time
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

/**
 * File (not directories) filter.
 *
 * @author paulburke
 */
var sFileFilter: FileFilter = FileFilter { file ->
    val fileName = file.name
    // Return files only (not directories) and skip hidden files
    file.isFile && !fileName.startsWith(HIDDEN_PREFIX)
}

/**
 * Folder (directories) filter.
 *
 * @author paulburke
 */
var sDirFilter: FileFilter = FileFilter { file ->
    val fileName = file.name
    // Return directories only and skip hidden directories
    file.isDirectory && !fileName.startsWith(HIDDEN_PREFIX)
}

fun getExtension(uri: String?): String? {
    if (uri == null) {
        return null
    }
    val dot = uri.lastIndexOf(".")
    return if (dot >= 0) {
        uri.substring(dot)
    } else {
        // No extension.
        ""
    }
}

/**
 * @return Whether the URI is a local one.
 */
fun isLocal(url: String?): Boolean {
    return url != null && !url.startsWith("http://") && !url.startsWith("https://")
}

/**
 * @return True if Uri is a MediaStore Uri.
 * @author paulburke
 */
fun isMediaUri(uri: Uri?): Boolean {
    return "media".equals(uri!!.authority, ignoreCase = true)
}

/**
 * Convert File into Uri.
 *
 * @param file
 * @return uri
 */
fun getUri(context: Context, file: File?): Uri? {
    if (file != null) {
        return FileProvider.getUriForFile(context, "$APPLICATION_ID.fileprovider", file)
    }
    return null
}

/**
 * Returns the path only (without file name).
 *
 * @param file
 * @return
 */
fun getPathWithoutFilename(file: File?): File? {
    if (file != null) {
        if (file.isDirectory) {
            // no file to be split off. Return everything
            return file
        } else {
            val filename = file.name
            val filepath = file.absolutePath

            // Construct path without file name.
            var pathwithoutname = filepath.substring(0,
                    filepath.length - filename.length)
            if (pathwithoutname.endsWith("/")) {
                pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length - 1)
            }
            return File(pathwithoutname)
        }
    }
    return null
}

/**
 * @return The MIME type for the given file.
 */
fun getMimeType(file: File): String? {
    val extension = getExtension(file.name)
    return if (extension!!.length > 0) MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1)) else "application/octet-stream"
}

/**
 * @return The MIME type for the give Uri.
 */
fun getMimeType(context: Context, uri: Uri): String? {
    val file = File(getPath(context, uri))
    return getMimeType(file)
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is [ ][paulburke]
 */
fun isLocalStorageDocument(uri: Uri): Boolean {
    return AUTHORITY == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 * @author paulburke
 */
fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 * @author paulburke
 */
fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 * @author paulburke
 */
fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param uri           The Uri to query.
 * @param selection     (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 * @author paulburke
 */
fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
            column
    )
    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                null)
        if (cursor != null && cursor.moveToFirst()) {
            if (BuildConfig.DEBUG) DatabaseUtils.dumpCursor(cursor)
            val column_index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(column_index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

/**
 * Get a file path from a Uri. This will get the the path for Storage Access
 * Framework Documents, as well as the _data field for the MediaStore and
 * other file-based ContentProviders.<br></br>
 * <br></br>
 * Callers should check whether the path is local before assuming it
 * represents a local file.
 *
 * @param context The context.
 * @param uri     The Uri to query.
 * @author paulburke
 * @see .isLocal
 * @see .getFile
 */
fun getPath(context: Context, uri: Uri): String? {
    if (BuildConfig.DEBUG) Log.d(
            TAG + " File -",
            "Authority: " + uri.authority +
                    ", Fragment: " + uri.fragment +
                    ", Port: " + uri.port +
                    ", Query: " + uri.query +
                    ", Scheme: " + uri.scheme +
                    ", Host: " + uri.host +
                    ", Segments: " + uri.pathSegments.toString()
    )
    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // LocalStorageProvider
        if (isLocalStorageDocument(uri)) {
            // The path is the id
            return DocumentsContract.getDocumentId(uri)
        } else if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }

            // TODO handle non-primary volumes
        } else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            if (("image" == type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if (("video" == type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if (("audio" == type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(
                    split[1]
            )
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {

        // Return the remote address
        return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }
    return uri.path
}

/**
 * Convert Uri into File, if possible.
 *
 * @return file A local file that the Uri was pointing to, or null if the
 * Uri is unsupported or pointed to a remote resource.
 * @author paulburke
 * @see .getPath
 */
fun getFile(context: Context, uri: Uri?): File? {
    if (uri != null) {
        val path = getPath(context, uri)
        if (path != null && isLocal(path)) {
            return File(path)
        }
    }
    return null
}

/**
 * Get the file size in a human-readable string.
 *
 * @param size
 * @return
 * @author paulburke
 */
fun getReadableFileSize(size: Int): String? {
    val BYTES_IN_KILOBYTES = 1024
    val dec = DecimalFormat("###.#")
    val KILOBYTES = " KB"
    val MEGABYTES = " MB"
    val GIGABYTES = " GB"
    var fileSize = 0f
    var suffix = KILOBYTES
    if (size > BYTES_IN_KILOBYTES) {
        fileSize = (size / BYTES_IN_KILOBYTES).toFloat()
        if (fileSize > BYTES_IN_KILOBYTES) {
            fileSize = fileSize / BYTES_IN_KILOBYTES
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES
                suffix = GIGABYTES
            } else {
                suffix = MEGABYTES
            }
        }
    }
    return (dec.format(fileSize.toDouble()) + suffix)
}

/**
 * Attempt to retrieve the thumbnail of given File from the MediaStore. This
 * should not be called on the UI thread.
 *
 * @param context
 * @param file
 * @return
 * @author paulburke
 */
fun getThumbnail(context: Context, file: File): Bitmap? {
    return getThumbnail(context, getUri(context, file), getMimeType(file))
}

/**
 * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
 * should not be called on the UI thread.
 *
 * @param context
 * @param uri
 * @return
 * @author paulburke
 */
fun getThumbnail(context: Context, uri: Uri): Bitmap? {
    return getThumbnail(context, uri, getMimeType(context, uri))
}

/**
 * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
 * should not be called on the UI thread.
 *
 * @param context
 * @param uri
 * @param mimeType
 * @return
 * @author paulburke
 */
fun getThumbnail(context: Context, uri: Uri?, mimeType: String?): Bitmap? {
    if (BuildConfig.DEBUG) Log.d(TAG, "Attempting to get thumbnail")
    if (!isMediaUri(uri)) {
        Log.e(TAG, "You can only retrieve thumbnails for images and videos.")
        return null
    }
    var bm: Bitmap? = null
    if (uri != null) {
        val resolver = context.contentResolver
        var cursor: Cursor? = null
        try {
            cursor = resolver.query(uri, null, null, null, null)
            if (cursor!!.moveToFirst()) {
                val id = cursor.getInt(0)
                if (BuildConfig.DEBUG) Log.d(TAG, "Got thumb ID: $id")
                if (mimeType!!.contains("video")) {
                    bm = MediaStore.Video.Thumbnails.getThumbnail(
                            resolver,
                            id.toLong(),
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            null)
                } else if (mimeType.contains(MIME_TYPE_IMAGE)) {
                    bm = MediaStore.Images.Thumbnails.getThumbnail(
                            resolver,
                            id.toLong(),
                            MediaStore.Images.Thumbnails.MINI_KIND,
                            null)
                }
            }
        } catch (e: java.lang.Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "getThumbnail", e)
        } finally {
            cursor?.close()
        }
    }
    return bm
}

/**
 * Get the Intent for selecting content to be used in an Intent Chooser.
 *
 * @return The intent for opening a file with Intent.createChooser()
 * @author paulburke
 */
fun createGetContentIntent(): Intent? {
    // Implicitly allow the user to select a particular kind of data
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    // The MIME data type filter
    intent.type = "*/*"
    // Only return URIs that can be opened with ContentResolver
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    return intent
}

fun isImage(file: File?): Boolean {
    if (file == null || !file.exists()) {
        return false
    }
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(file.path, options)
    return options.outWidth != -1 && options.outHeight != -1
}

fun getFileSize(length: Double): String? {
    if (length > MiB) {
        return format.format(length / MiB) + "MB"
    }
    return if (length > KiB) {
        format.format(length / KiB) + "KB"
    } else format.format(length) + "B"
}

fun resImageToFile(mActivity: Activity, mFileDirectory: File?, mFileName: String, mImage: Int): File? {
    val mResId = mActivity.resources
            .getIdentifier("god_$mImage", "drawable", mActivity.packageName)
    val imageUri: Uri = getUriForDrawable(mActivity, mResId)
    try {
        val mFile: File = mSaveInputStreamToFile(mActivity, mFileDirectory, mFileName, mActivity.contentResolver.openInputStream(imageUri)!!)!!
        Log.i(TAG, "mThroughImage: mFile " + mFile.absolutePath)
        return mFile
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }
}

fun mSaveInputStreamToFile(mContext: Context?, mFileDirectory: File?, mFileName: String?, input: InputStream): File? {
    return try {
        val file = File(mFileDirectory, mFileName)
        file.mkdirs()
        if (file.exists()) {
            file.delete()
        }
        val output: OutputStream = FileOutputStream(file)
        try {
            val buffer = ByteArray(4 * 1024) // or other buffer size
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
        } finally {
            output.close()
        }
        input.close()
        file
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }
}

fun saveTempBitmap(mContext: Context?, externalCacheDir: File?, mFileName: String?, mBitmap: Bitmap): File? {
    val file = File(externalCacheDir, mFileName)
    file.mkdirs() // don't forget to make the directory
    if (file.exists()) {
        file.delete()
    }
    val stream = FileOutputStream(file) // overwrites this image every time
    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.close()
    return file
}

fun addWatermark(mContext: Context, mFileDirectory: File?, tempImageName: String?, mWaterMarkRes: Int, mFile: File): File? {
    val w: Int
    val h: Int
    val c: Canvas
    val paint: Paint
    val bmp: Bitmap
    val watermark: Bitmap
    val matrix: Matrix
    val scale: Float
    val r: RectF
    val source = BitmapFactory.decodeFile(mFile.absolutePath)
    w = source.width
    h = source.height
    // Create the new bitmap
    bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
    // Copy the original bitmap into the new one
    c = Canvas(bmp)
    c.drawBitmap(source, 0f, 0f, paint)
    // Load the watermark
    watermark = BitmapFactory.decodeResource(mContext.resources, mWaterMarkRes)
    // Scale the watermark to be approximately 40% of the source image height
    scale = (h.toFloat() * 0.40 / watermark.height.toFloat()).toFloat()
    // Create the matrix
    matrix = Matrix()
    matrix.postScale(scale, scale)
    // Determine the post-scaled size of the watermark
    r = RectF(0F, 0F, watermark.width.toFloat(), watermark.height.toFloat())
    matrix.mapRect(r)
    // Move the watermark to the bottom right corner
    matrix.postTranslate(w - r.width(), h - r.height())
    // Draw the watermark
    c.drawBitmap(watermark, matrix, paint)
    // Free up the bitmap memory
    watermark.recycle()
    return saveTempBitmap(mContext, mFileDirectory, tempImageName, bmp)
}

fun getZipFileContent(zipFilePath: String): MutableList<String> {
    val mutableList = mutableListOf<String>()
    try {
        val zipFile = ZipFile(zipFilePath)
        val entries: Enumeration<out ZipEntry> = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val name = entry.name
            val compressedSize = entry.compressedSize
            val normalSize = entry.size
            val type = if (entry.isDirectory) "DIR" else "FILE"
            if (!entry.isDirectory) {
                mutableList.add(name)
            }
            //println(name)
            //System.out.format("\t %s - %d - %d\n", type, compressedSize, normalSize)
        }
        zipFile.close()
    } catch (ex: IOException) {
        System.err.println(ex)
    }
    return mutableList
}

fun Context.notifyFileChanges(file: File) {
    MediaScannerConnection.scanFile(this, arrayOf(file.path), arrayOf("*/*")) { _, uri ->

    }
}

fun okioFileDownload(url: String, destFile: File): @NonNull Flowable<Pair<Boolean, Any>> {
    return Flowable.create<Pair<Boolean, Any>>({ emitter ->
        var sink: BufferedSink? = null
        var source: BufferedSource? = null
        val lastProgress = 0
        try {
            val request = Request.Builder().url(url).build()
            val response = OkHttpClient().newCall(request).execute()
            val body = response.body()
            val contentLength = Objects.requireNonNull(body)?.contentLength()
            source = body!!.source()
            sink = Okio.buffer(Okio.sink(destFile))
            val sinkBuffer = sink.buffer()
            var totalBytesRead: Long = 0
            val bufferSize = 6 * 1024
            var bytesRead: Long
            while (source.read(sinkBuffer, bufferSize.toLong()).also { bytesRead = it } != -1L) {
                sink.emit()
                totalBytesRead += bytesRead
                val progress = (totalBytesRead * 100 / contentLength!!).toInt()
                if (lastProgress != progress) { //reduce_redundant_callback
                    emitter.onNext(Pair(false, progress))
                } else {
                    emitter.onNext(Pair(true, destFile))
                }
            }
            sink.flush()
        } catch (e: IOException) {
            Log.e(TAG, "IOException --- ", e)
            emitter.onError(e)
        } finally {
            Util.closeQuietly(sink)
            Util.closeQuietly(source)
        }
        emitter.onComplete()
    }, BackpressureStrategy.DROP)

}

@Throws(IOException::class)
 fun download(
        url: @NonNull String,
        destFile: @NonNull File
): @NonNull Flowable<Triple<Boolean, Long, File>> {
     return Flowable.create({ emitter ->
         val request = Request.Builder().url(url).build()
         val response: Response = OkHttpClient().newCall(request).execute()
         val body: ResponseBody = response.body()!!
         val contentLength = body.contentLength()
         val source = body.source()
         val sink = Okio.buffer(Okio.sink(destFile))
         val sinkBuffer: Buffer = sink.buffer()
         var totalBytesRead: Long = 0
         val bufferSize = 8 * 1024
         var bytesRead: Long
         while (source.read(sinkBuffer, bufferSize.toLong()).also { bytesRead = it } != -1L) {
             sink.emit()
             totalBytesRead += bytesRead
             val progress = (totalBytesRead * 100 / contentLength)
             emitter.onNext(Triple(false,totalBytesRead,destFile))
         }
         sink.flush()
         sink.close()
         source.close()
         emitter.onNext(Triple(true,100,destFile))
    }, BackpressureStrategy.DROP)
}