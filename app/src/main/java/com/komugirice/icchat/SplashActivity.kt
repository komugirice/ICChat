package com.komugirice.icchat

import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.firebase.firebaseFacade

class SplashActivity : BaseActivity() {

    val SPLASH_TIME = 1000L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if(FirebaseAuth.getInstance().currentUser != null) {
            firebaseFacade.initManager() {
                Handler().postDelayed({
                    finishAffinity()
                    MainActivity.start(this)
                }, SPLASH_TIME)
            }
        } else {
            Handler().postDelayed({
                LoginActivity.start(this)
            }, SPLASH_TIME)
        }
    }
}
