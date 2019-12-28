package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.komugirice.icchat.firestore.model.Room
import kotlinx.android.synthetic.main.activity_add_friend.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.backImageView

class AddFriendActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
    }

    private fun initClick() {
        // <ボタン
        backImageView.setOnClickListener {
            this.onBackPressed()
        }

        readQRCodeButton.setOnClickListener{

        }

        displayQRCodeButton.setOnClickListener {

        }

    }
    companion object {
        fun start(context: Context?) =
            context?.startActivity(Intent(context, AddFriendActivity::class.java))
    }
}
