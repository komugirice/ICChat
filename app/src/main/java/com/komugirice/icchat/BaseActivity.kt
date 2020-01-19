package com.komugirice.icchat

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.komugirice.icchat.firebase.firebaseFacade
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.util.Prefs

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

    fun logout(){
        UserStore.updateFcmToken(null){
            Prefs().fcmToken.remove()
            Prefs().hasToUpdateFcmToken.put(true)
            firebaseFacade.clearManager()
        }

    }

}