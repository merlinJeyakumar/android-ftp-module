package com.nativedevps.ftp.client.cache

import com.nativedevps.ftp.model.FtpFileModel

open class FilesCache {
    private val indexList = mutableListOf<String>()
    private val hashMap = mutableMapOf<String, List<FtpFileModel>>()
    private var currentIndex = 0

    fun pushElement(path: String, list: List<FtpFileModel>) {
        hashMap[path] = list
        if (!indexList.contains(path)) {
            indexList.add(path)
            currentIndex += 1
        }
    }

    fun getElement(path: String): List<FtpFileModel>? {
        return if (hashMap.containsKey(path)) hashMap[path] else null
    }

    fun getLastElement(): List<FtpFileModel>? {
        if (currentIndex >= 0 && indexList.isNotEmpty()) {
            val scanIndex = if (currentIndex == 0) 0 else currentIndex - 1
            return hashMap[indexList[scanIndex]]
        } else {
            println("getLastElement There are no element to retrieve")
        }
        return null
    }

    fun moveCursorTop() {
        if (currentIndex >= 0 && indexList.isNotEmpty()) {
            currentIndex -= 1
        }
    }

    fun dump() {
        hashMap.clear()
        indexList.clear()
        currentIndex = 0
    }

}