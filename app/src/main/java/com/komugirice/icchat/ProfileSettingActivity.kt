package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.qiitaapplication.extension.getDateToString
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.data.firestore.manager.UserManager
import kotlinx.android.synthetic.main.activity_profile_setting.*

class ProfileSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)
        initialize()
    }

    private fun initialize() {
        initLayout()
        initClick()
    }

    private fun initLayout() {
        // TODO UserManager.myUserの設定
        val myUser = UserManager.myUser
        userId.text = myUser.userId
        userName.text = myUser.name
        birthDay.text = myUser.birthDay?.also {it.getDateToString()}.run{"設定なし"}
    }

    private fun initClick() {
        backImageView.setOnClickListener {
            finish()
        }
    }

    companion object {
        fun start(context: Context?) =
            context?.startActivity(
                Intent(context, ProfileSettingActivity::class.java)
            )
    }
}
