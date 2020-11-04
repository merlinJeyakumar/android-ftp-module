package com.support.utills

import timber.log.Timber.DebugTree
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class FileLoggingTree(private val logFile: File) : DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String?,
        t: Throwable?
    ) {
        try {
            val path = "Log"
            val fileNameTimeStamp = SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(Date())
            val logTimeStamp = SimpleDateFormat(
                "E MMM dd yyyy 'at' hh:mm:ss:SSS aaa",
                Locale.getDefault()
            ).format(Date())
            val fileName = "$fileNameTimeStamp.html"

            // Create file
            val file = logFile
            file.parentFile.mkdirs()
            file.createNewFile()
            val stringBuilder = StringBuilder()

            // If file created or exists save logs
            val writer = FileWriter(file, true)
            stringBuilder.append("\n")
                .append(logTimeStamp)
                .append("\n")
                .append(tag)
                .append("\n")
                .append(message)
                .append("\n--")

            writer.append(stringBuilder.toString())

            writer.flush()
            writer.close()
        } catch (e: Exception) {
            Log.e(
                LOG_TAG,
                "Error while logging into file : $e"
            )
        }
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        // Add log statements line number to the log
        return super.createStackElementTag(element) + " - " + element.lineNumber
    }

    companion object {
        private val LOG_TAG = FileLoggingTree::class.java.simpleName
    }

}