package com.komugirice.icchat

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.komugirice.icchat.R

class VersionUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_up)

        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_version_up))
            .setPositiveButton(R.string.install, object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googlePlayUrl))
                    startActivity(intent)
                }
            })
                // アプリを消すことが出来ないので除去
//            .setNegativeButton(R.string.close_button, object: DialogInterface.OnClickListener {
//                override fun onClick(dialog: DialogInterface?, which: Int) {
//                    finishAffinity()
//                    finishAndRemoveTask()
//                }
//            })
            .show()
    }

    companion object {
        val googlePlayUrl = "https://play.google.com/store/apps/details?id=com.komugirice.icchat"
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, VersionUpActivity::class.java)
            )
    }
}
