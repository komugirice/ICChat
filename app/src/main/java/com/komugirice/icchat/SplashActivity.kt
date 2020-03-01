package com.komugirice.icchat

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.komugirice.icchat.ICChatApplication.Companion.isFacebookAuth
import com.komugirice.icchat.ICChatApplication.Companion.isGoogleAuth
import com.komugirice.icchat.extension.getVersion
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.util.FireStoreUtil
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.util.*

class SplashActivity : BaseActivity() {

    val SPLASH_TIME = 1000L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // ログイン連携有無の設定
        val prividerIdList = FirebaseAuth.getInstance().currentUser?.providerData?.map { it.providerId } ?: listOf()
        isGoogleAuth = prividerIdList.contains(GoogleAuthProvider.PROVIDER_ID)
        isFacebookAuth = prividerIdList.contains(FacebookAuthProvider.PROVIDER_ID)

        if (FirebaseAuth.getInstance().currentUser != null) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            Timber.d("uid:$uid)")

            // 多重ログインチェックが必要
            // TODO isAlreadyLoginを実装するには、アプリ消されたらloginDateをnullにする処理が必要
//            UserStore.isAlreadyLogin(onFailed()) {
//                if (it) {
//                    // 別のユーザがログイン済みです
//                    Toast.makeText(
//                        this,
//                        getString(R.string.login_failed_already),
//                        Toast.LENGTH_LONG
//                    ).show()
//                    FirebaseAuth.getInstance().signOut()
//                    LoginActivity.signOutProvider()
//                    startLoginActivity()
//                    return@isAlreadyLogin
//                }
                // ログイン日時更新
                UserStore.updateLoginDateTime(Date()){
                    // 次の画面に遷移
                    startMainActivity()

                }
//            }


        } else {
            Handler().postDelayed({
                LoginActivity.start(this)
            }, SPLASH_TIME)
        }
    }

    private fun onFailed() = {
        // uidとユーザが紐付いていない場合、onSuccessしない場合に通過する処理の定義
        // ユーザ情報が無いのでログインできませんでした
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.login_failed_no_user_splash),
            Toast.LENGTH_LONG
        ).show()
        LoginActivity.signOutProvider()
        FirebaseAuth.getInstance().signOut()
        startLoginActivity()
    }

    private fun startLoginActivity() {
        LoginActivity.start(this)
    }

    private fun startMainActivity() {
        // バージョンチェック
        FireStoreUtil.getVersion {
            val minVersion = it
            Timber.d("checkVersionUp minVersion:${minVersion.getVersion()} currentVersion:${BuildConfig.VERSION_NAME.getVersion()}")
            if (minVersion.getVersion() > BuildConfig.VERSION_NAME.getVersion()) {
                Toast.makeText(this, "アプリをアップデートしてください", Toast.LENGTH_LONG).show()
                return@getVersion
            }

            FirebaseFacade.initManager(onFailed()) {
                Handler().postDelayed({
                    finishAffinity()
                    MainActivity.start(this)
                }, SPLASH_TIME)
            }
        }
    }

}

