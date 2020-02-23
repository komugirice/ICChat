package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.util.FcmUtil
import kotlinx.android.synthetic.main.activity_create_user_complete.*

class CreateUserCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user_complete)
        initClick()
    }

    private fun initClick() {
        loginButton.setOnClickListener {
            // Manager初期化
            FirebaseFacade.initManager {
                // FCM初期化
                FcmUtil.initFcm()
                // メイン画面に遷移
                MainActivity.start(this)

                //Complete and destroy login activity once successful
                finish()
            }
        }
    }

    companion object {
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, CreateUserCompleteActivity::class.java)
            )
    }
}
