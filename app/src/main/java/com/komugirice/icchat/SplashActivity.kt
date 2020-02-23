package com.komugirice.icchat

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.komugirice.icchat.ICChatApplication.Companion.isFacebookAuth
import com.komugirice.icchat.ICChatApplication.Companion.isGoogleAuth
import com.komugirice.icchat.extension.getVersion
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.util.FireStoreUtil
import timber.log.Timber

class SplashActivity : BaseActivity() {

    val SPLASH_TIME = 1000L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // ログイン連携有無の設定
        val prividerIdList = FirebaseAuth.getInstance().currentUser?.providerData?.map { it.providerId } ?: listOf()
        isGoogleAuth = prividerIdList.contains(GoogleAuthProvider.PROVIDER_ID)
        isFacebookAuth = prividerIdList.contains(FacebookAuthProvider.PROVIDER_ID)

        // TODO ネットワークエラー対応が必要
        if (FirebaseAuth.getInstance().currentUser != null) {
            val userInfo = FirebaseAuth.getInstance().currentUser?.providerData?.first()?.providerId
            Timber.d("$userInfo)")
            // バージョンチェック
            FireStoreUtil.getVersion {
                val minVersion = it
                Timber.d("checkVersionUp minVersion:${minVersion.getVersion()} currentVersion:${BuildConfig.VERSION_NAME.getVersion()}")
                if (minVersion.getVersion() > BuildConfig.VERSION_NAME.getVersion()) {
                    Toast.makeText(this, "アプリをアップデートしてください", Toast.LENGTH_LONG).show()
                    return@getVersion
                }

                FirebaseFacade.initManager() {
                    Handler().postDelayed({
                        finishAffinity()
                        MainActivity.start(this)
                    }, SPLASH_TIME)
                }
            }
        } else {
            Handler().postDelayed({
                LoginActivity.start(this)
            }, SPLASH_TIME)
        }
    }

}

