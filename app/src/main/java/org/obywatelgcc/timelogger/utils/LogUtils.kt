package org.obywatelgcc.timelogger.utils

import android.util.Log

fun<T: Any> T.logDebug(message: String, tag: String = javaClass.simpleName) {
    Log.d(tag, message)
}

fun<T: Any> T.logInfo(message: String, tag: String = javaClass.simpleName) {
    Log.i(tag, message)
}