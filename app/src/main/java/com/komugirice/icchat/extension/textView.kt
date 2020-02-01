package com.komugirice.icchat.extension

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import com.komugirice.icchat.R

@BindingAdapter("intHoge", "stringFoo")
fun TextView.hogeFoo(hoge: Int?, foo: String?) {
    if (hoge == null || foo == null)
        return
    val stringBuilder = StringBuilder()
    for (i in 0 until hoge)
        stringBuilder.append(foo)
    this.text = stringBuilder.toString()

}

@BindingAdapter("isSelected")
fun TextView.setSelectedUser(isSelected: Boolean?) {
    if (isSelected == true) {
        this.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        this.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.gray))
    } else {
        this.setBackgroundResource(R.drawable.text_background_color_gray)
    }
}