package com.komugirice.icchat

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.extension.getVersion
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.util.FireStoreUtil
import timber.log.Timber

class SplashActivity : BaseActivity() {

    val SPLASH_TIME = 1000L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // TODO ネットワークエラー対応が必要
        if (FirebaseAuth.getInstance().currentUser != null) {
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

