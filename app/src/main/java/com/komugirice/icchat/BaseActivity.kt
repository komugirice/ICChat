package com.komugirice.icchat

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.komugirice.icchat.databinding.ProgressDialogBinding
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.util.Prefs

/**
 * Created by Jane on 2018/04/05.
 */
abstract class BaseActivity : AppCompatActivity() {

    private var progressDialog: MaterialDialog? = null

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
        ICChatApplication.isFacebookAuth = false  // プロフィール設定画面で使う
        ICChatApplication.isGoogleAuth = false    // プロフィール設定画面で使う
        UserStore.updateFcmToken(null){
            Prefs().fcmToken.remove()
            Prefs().hasToUpdateFcmToken.put(true)
            // TODO プロフィール設定画面のFacebook連携処理でおかしくなるので応急処置
            // FirebaseFacade.clearManager()
        }

    }

    protected fun showProgressDialog(context: Context){
        dismissProgressDialog()
        this.progressDialog =  MaterialDialog(context).apply {
            cancelable(true)
            val dialogBinding = ProgressDialogBinding.inflate(
                LayoutInflater.from(context),
                null,
                false
            )
            setContentView(dialogBinding.root)
        }
        // 背景を透過
        this.progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.progressDialog?.window?.setDimAmount(0.0f)
        this.progressDialog?.show()
    }

    protected fun dismissProgressDialog(){
        this.progressDialog?.dismiss()
    }



}