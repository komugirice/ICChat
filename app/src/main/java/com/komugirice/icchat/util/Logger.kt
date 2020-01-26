package com.komugirice.icchat.util

import android.util.Log

class Logger {

    companion object {
        fun d(log: String) = outputLog(Log.DEBUG, log)

        private fun outputLog(logLevel: Int, log: String) {
            val tag = "ICChat"
            when(logLevel) {
                Log.DEBUG -> Log.d(tag, log)
            }
        }
    }
}