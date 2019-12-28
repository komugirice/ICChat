package com.komugirice.icchat.util

import com.komugirice.icchat.R
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.qiitaapplication.extension.HHmmToString
import com.example.qiitaapplication.extension.compareDate
import com.example.qiitaapplication.extension.yyyyMMddHHmmToString
import com.komugirice.icchat.extension.setRoundedImageView
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.util.ICChatUtil.loadUserIconImage
import com.squareup.picasso.Picasso
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
    fun TextView.getDateTime(dateTime: Date?) {
        if (dateTime == null) return

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

    /**
     * xmlでImageViewに:imageUrlを設定すると画像が取得できる
     *
     * @param url
     *
     */
    @JvmStatic
    @BindingAdapter("imageUrl")
    fun ImageView.loadImage(url: String?) {
        Picasso.get().load(url).into(this)
    }

    /**
     * ユーザアイコンを設定する
     *
     * @param url
     *
     */
    @JvmStatic
    @BindingAdapter("userIconImageUrl")
    fun ImageView.loadUserIconImage(userId: String) {
        FireStorageUtil.getUserIconImage(userId) {
            this.setRoundedImageView(it)
        }
    }

    /**
     * RoomFragmentにnameを設定する
     *
     * @param url
     *
     */
    @JvmStatic
    @BindingAdapter("setRoomName")
    fun TextView.setRoomName(room: Room) {
        // ルーム名を設定する
        var text: String
        // シングルルームの場合はルーム名をユーザ名にする
        if(room.isGroup == false) {
            // シングルルームの場合
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
     * RoomFragmentにアイコン画像を設定する
     *
     * @param url
     *
     */
    @JvmStatic
    @BindingAdapter("roomIconImageUrl")
    fun ImageView.loadRoomIconImage(room: Room) {
        this.setImageDrawable(null)

        // シングルルームとグループルームで分岐
        if(room.isGroup == false) {
            // シングルルームの場合
            val friendId  = room.userIdList.filter{ !it.equals(UserManager.myUserId) }.first()
            FireStorageUtil.getUserIconImage(friendId) {
                this.setRoundedImageView(it)
            }
        } else {
            // グループルームの場合
            FireStorageUtil.getGroupIconImage(room.documentId) {
                this.setRoundedImageView(it)
            }
        }
    }

}