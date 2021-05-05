package com.support.device.network.ftp.server

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE


object ClipBoardHelper {
    fun Context.copyTextOnClipBoard(label:String="label", text:String){
        val clipboard: ClipboardManager? = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val clip: ClipData = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }
}