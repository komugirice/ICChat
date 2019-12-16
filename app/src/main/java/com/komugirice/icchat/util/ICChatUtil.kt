package com.komugirice.icchat.util

import com.komugirice.icchat.R
import android.text.Html
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.qiitaapplication.extension.HHmmToString
import com.example.qiitaapplication.extension.compareDate
import com.example.qiitaapplication.extension.yyyyMMddHHmmToString
import java.util.*


object ICChatUtil {

    /**
     * xmlでTextViewに:dateTimeを設定するとyyyy/mm/dd hh:mmが取得できる
     *
     * @param url
     *
     */
    @JvmStatic
    @BindingAdapter("dateTime")
    fun TextView.getDateTime(dateTime: Date) {
        // 本日日付と比較
        if(dateTime.compareDate(Date()))
            this.text = dateTime.HHmmToString()
        else
            this.text = dateTime.yyyyMMddHHmmToString()
    }
    /**
     * TextViewに必須マークを表示
     *
     * @param textView
     * @param enable
     *
     */
    @JvmStatic
    @BindingAdapter("requiredMarkVisible")
    fun TextView.requiredMarkVisible(enable: Boolean) {
        val text = this.text.toString()
        val requiredMark =
            " " + this.context.getString(R.string.required_mark)
        if (enable) {
            this.text = Html.fromHtml("$text<font color=\"#e86242\">$requiredMark</font>")
        } else {
            val defaultText = text.replace(requiredMark, "")
            this.text = defaultText
        }
    }
}