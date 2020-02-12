package com.komugirice.icchat.extension

import android.text.Html
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.gson.Gson
import com.komugirice.icchat.R
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Request
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.util.CommonUtil
import timber.log.Timber
import java.util.*

/**
 * xmlでTextViewに:dateTimeを設定するとyyyy/mm/dd hh:mmが取得できる
 *
 * @param url
 *
 */
@BindingAdapter("dateTime")
fun TextView.getDateTime(dateTime: Date?) {
    if (dateTime == null) return

    // 本日日付と比較
    if(dateTime.compareDate(Date()))
        this.text = dateTime.HHmmToString()
    else
        this.text = dateTime.yyyyMMddHHmmToString()
}

/**
 * xmlでTextViewに:dateJPを設定するとyyyy年mm月dd日(曜日)が取得できる
 *
 * @param url
 *
 */
@BindingAdapter("dateJP")
fun TextView.getDateJp(dateTime: Date?) {
    if (dateTime == null) return

    val today = Calendar.getInstance().time
    val yesterday = Calendar.getInstance().run{
            add(Calendar.DAY_OF_MONTH, -1)
            time
        }

    if(today.getDateToString() == dateTime.getDateToString()) {
        this.text = "今日(${CommonUtil.getDayOfWeek(dateTime)})"
    } else if(yesterday.getDateToString() == dateTime.getDateToString()) {
        this.text = "昨日(${CommonUtil.getDayOfWeek(dateTime)})"
    } else {
        this.text = "${dateTime.getJPDateToString()}(${CommonUtil.getDayOfWeek(dateTime)})"
    }
}

/**
 * xmlでTextViewに:timeを設定するとhh:mmが取得できる
 *
 * @param url
 *
 */
@BindingAdapter("time")
fun TextView.getTime(dateTime: Date?) {
    if (dateTime == null) return

    this.text = dateTime.HHmmToString()
}

/**
 * TextViewに必須マークを表示
 *
 * @param textView
 * @param enable
 *
 */
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

/**
 * RoomFragmentにnameを設定する
 *
 * @param room
 *
 */
@BindingAdapter("setRoomName")
fun TextView.setRoomName(room: Room?) {
    if(room == null) return
    // ルーム名を設定する
    var text: String
    // シングルルームの場合はルーム名をユーザ名にする
    if(room.isGroup == false) {
        // シングルルームの場合
        Timber.d(Gson().toJson(room))
        val friendId  = room.userIdList.filter{ !it.equals(UserManager.myUserId) }.first()
        val friend = UserManager.myFriends.filter{ it.userId.equals(friendId)}.first()
        // ルーム名をユーザ名にする
        text = friend.name
    } else {
        text = room.name + "(${room.userIdList.size})"

    }
    this.text = text
}

/**
 * Requestインスタンスからリクエストしたユーザ名を設定する
 *
 * @param url
 *
 */
@BindingAdapter("setRequesterName")
fun TextView.setRequesterName(request: Request) {
    if(request == null) return
    // リクエスト名を設定する
    val requester = UserManager.allUsers.filter{ it.userId.equals(request.requesterId)}.first()
    this.text = requester.name
}

/**
 * Requestインスタンスからリクエストされたユーザ名を設定する
 *
 * @param url
 *
 */
@BindingAdapter("setBeRequestedName")
fun TextView.setBeRequestedName(request: Request) {
    if(request == null) return
    // リクエスト名を設定する
    val requested = UserManager.allUsers.filter{ it.userId.equals(request.beRequestedId)}.first()
    this.text = requested.name
}