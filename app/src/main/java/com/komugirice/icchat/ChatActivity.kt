package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.komugirice.icchat.data.firestore.Room

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initialize()
    }

    /**
     * initializeメソッド
     *
     */
    private fun initialize() {
        initLayout()
    }

    /**
     * initLayoutメソッド
     *
     */
    private fun initLayout() {
        initClick()
    }

    /**
     * initClickメソッド
     *
     */
    private fun initClick() {

    }

    companion object {
        private const val KEY_ROOM = "key_room"
        fun start(context: Context?) =
            context?.startActivity(
                Intent(context, ChatActivity::class.java)
            )
//        fun start(context: Context?, room: Room ) =
//            context?.startActivity(
//                Intent(context, ChatActivity::class.java)
//                    .putExtra(KEY_ROOM, room)
//            )
    }
}
