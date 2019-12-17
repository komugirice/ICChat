package com.komugirice.icchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.ui.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    val SPLASH_TIME = 2000L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if(FirebaseAuth.getInstance().currentUser != null) {
            UserManager.initUserManager()
            Handler().postDelayed({
                MainActivity.start(this)
            }, SPLASH_TIME)
        } else {
            Handler().postDelayed({
                LoginActivity.start(this)
            }, SPLASH_TIME)
        }
    }
}
