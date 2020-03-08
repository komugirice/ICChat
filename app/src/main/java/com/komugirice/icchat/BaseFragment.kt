package com.komugirice.icchat

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.komugirice.icchat.databinding.ProgressDialogBinding

abstract class BaseFragment : Fragment() {

    private var progressDialog: MaterialDialog? = null

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