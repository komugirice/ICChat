package com.komugirice.icchat

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.komugirice.icchat.firestore.manager.RequestManager
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import kotlinx.android.synthetic.main.activity_chat.*

abstract class BaseActivity : AppCompatActivity() {

    /**
     * hideKeybordメソッド
     *
     */
    protected fun hideKeybord(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    protected fun initManager(onSuccess: () -> Unit) {
        UserManager.initUserManager() {
            RoomManager.initRoomManager() {
                RequestManager.initRequestManager() {
                    onSuccess.invoke()
                }
            }
        }
    }
}