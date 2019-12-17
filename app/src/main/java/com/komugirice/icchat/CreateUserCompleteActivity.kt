package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.data.firestore.model.User
import com.komugirice.icchat.data.firestore.store.UserStore
import kotlinx.android.synthetic.main.activity_create_user_complete.*

class CreateUserCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user_complete)
        initClick()
    }

    private fun initClick() {
        loginButton.setOnClickListener {
            // ユーザ情報取得
            UserStore.getLoginUser {
                it.result?.toObjects(User::class.java)?.firstOrNull().also {

                    it?.also {
                        // UserManager初期化
                        UserManager.initUserManager(it)
                        // メイン画面に遷移
                        MainActivity.start(this)

                        //Complete and destroy login activity once successful
                        finish()
                    }
                }

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
