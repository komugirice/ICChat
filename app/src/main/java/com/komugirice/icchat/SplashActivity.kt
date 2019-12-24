package com.komugirice.icchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager

class SplashActivity : AppCompatActivity() {

    val SPLASH_TIME = 1000L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if(FirebaseAuth.getInstance().currentUser != null) {
            UserManager.initUserManager() {
                RoomManager.initRoomManager() {
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
