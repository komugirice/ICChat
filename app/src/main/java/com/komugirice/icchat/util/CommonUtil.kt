package com.komugirice.icchat.util

import java.text.SimpleDateFormat
import java.util.*

class CommonUtil {
    companion object {
        /**
         * 曜日を取得
         * @param date
         * @return 曜日
         */
        fun getDayOfWeek(date: Date?): String? {
            val weekdays: Array<String> = arrayOf("日", "月", "火", "水", "木", "金", "土")
            if(date == null) return null

            return SimpleDateFormat("E").format(date)
        }
    }
}