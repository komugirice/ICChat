package com.komugirice.icchat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.komugirice.icchat.ICChatApplication.Companion.isFacebookAuth
import com.komugirice.icchat.ICChatApplication.Companion.isGoogleAuth
import com.komugirice.icchat.extension.getDomainFromEmail
import com.komugirice.icchat.extension.getVersion
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.util.FireStoreUtil
import com.komugirice.icchat.util.ICChatUtil
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.lang.Math.log
import java.util.*
import kotlin.math.log

class SplashActivity : BaseActivity() {

    val SPLASH_TIME = 1000L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // ログイン連携有無の設定
        val prividerIdList = FirebaseAuth.getInstance().currentUser?.providerData?.map { it.providerId } ?: listOf()
        isGoogleAuth = prividerIdList.contains(GoogleAuthProvider.PROVIDER_ID)
        isFacebookAuth = prividerIdList.contains(FacebookAuthProvider.PROVIDER_ID)

        // キャッシュディレクトリ
        ICChatUtil.deleteCacheDir()

        if (FirebaseAuth.getInstance().currentUser != null) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            Timber.d("uid:$uid)")

            // 多重ログインチェックが必要
//            // TODO isAlreadyLoginを実装するには、アプリ消されたらloginDateをnullにする処理が必要
//            UserStore.isAlreadyLogin(onFailed(), {
//                if (it) {
//                    val mail = FirebaseAuth.getInstance().currentUser?.email
//                    // 暫定対応としてテストユーザのみ多重ログイン制御
//                    if (mail?.getDomainFromEmail() == "example.com") {
//                        // 別のユーザがログイン済みです
//                        Toast.makeText(
//                            this,
//                            getString(R.string.login_failed_already),
//                            Toast.LENGTH_LONG
//                        ).show()
//                        FirebaseAuth.getInstance().signOut()
//                        LoginActivity.signOutProvider()
//                        startLoginActivity()
//                        return@isAlreadyLogin
//                    }
                    // ログイン日時更新
                    UserStore.updateLoginDateTime(Date()) {
                        // 次の画面に遷移
                        startMainActivity()

                    }
//                }
//            })


        } else {
            Handler().postDelayed({
                startLoginActivity()
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
        val adminMail = BuildConfig.ADMIN_MAIL
        val adminPassword = BuildConfig.ADMIN_PASSWORD
        FirebaseAuth.getInstance().signInWithEmailAndPassword(adminMail, adminPassword)
            .addOnCompleteListener {
                checkVersion(){
                    FirebaseAuth.getInstance().signOut()
                    LoginActivity.start(this)
                }
        }

    }

    private fun startMainActivity() {
        checkVersion(){
            FirebaseFacade.initManager(onFailed()) {
                Handler().postDelayed({
                    finishAffinity()
                    MainActivity.start(this)
                }, SPLASH_TIME)
            }
        }
    }

    private fun checkVersion(onSuccess: () -> Unit ) {

        // バージョンチェック
        FireStoreUtil.getVersion {
            val minVersion = it
            Timber.d("checkVersionUp minVersion:${minVersion.getVersion()} currentVersion:${BuildConfig.VERSION_NAME.getVersion()}")
            if (minVersion.getVersion() > BuildConfig.VERSION_NAME.getVersion()) {
//              // バージョン更新画面へ遷移させる
                VersionUpActivity.start(this)
            } else {
                onSuccess.invoke()
            }
        }
    }

//    private fun promptVersionUp() {
//        // Creates instance of the manager.
//        val appUpdateManager = AppUpdateManagerFactory.create(this)
//
//        // Returns an intent object that you use to check for an update.
//        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
//
//        // Checks that the platform will allow the specified type of update.
//        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
//            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
//                // For a flexible update, use AppUpdateType.FLEXIBLE
//                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
//            ) {
//                // Request the update.
//                appUpdateManager.startUpdateFlowForResult(
//                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
//                    appUpdateInfo,
//                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
//                    AppUpdateType.IMMEDIATE,
//                    // The current activity making the update request.
//                    this,
//                    // Include a request code to later monitor this update request.
//                    MY_REQUEST_CODE)
//
//            }
//        }
//
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MY_REQUEST_CODE) {
//            if (resultCode != RESULT_OK) {
//                Timber.d("Update flow failed! Result code: $resultCode")
//                // If the update is cancelled or fails,
//                // you can request to start the update again.
//            }
//        }
//    }
//
//
//
//    companion object {
//        const val MY_REQUEST_CODE = 1001
//    }
}

