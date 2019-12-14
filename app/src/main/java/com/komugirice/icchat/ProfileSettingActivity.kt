package com.komugirice.icchat

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import com.example.qiitaapplication.extension.getDateToString
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.manager.UserManager
import kotlinx.android.synthetic.main.activity_profile_setting.*
import java.util.*

class ProfileSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        initialize()
    }

    private fun initialize() {
        initLayout()
        initClick()
    }

    private fun initLayout() {
        // TODO UserManager.myUserの設定
        val myUser = UserManager.myUser
        userId.text = myUser.userId
        userName.text = myUser.name ?: "設定なし"
        birthDay.text = myUser.birthDay?.getDateToString() ?: "設定なし"
    }

    private fun initClick() {
        backImageView.setOnClickListener {
            finish()
        }

        userName.setOnClickListener {
            UserNameActivity.start(this)
        }

        birthDay.setOnClickListener {
            showDateDialog()
        }

    }

    private fun showDateDialog() {
        val dialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                updateBirthDay(year, month, dayOfMonth)
            }
        }, 2000, 1, 1)
        dialog.show()
    }

    fun updateBirthDay(year: Int, month: Int, dayOfMonth: Int) {
        val birthDay = Calendar.getInstance().run {
            set(year, month, dayOfMonth, 0, 0, 0)
            time
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(UserManager.myUser.documentId)
            .update("birthDay", birthDay)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    UserManager.myUser.birthDay = birthDay
                    initLayout()
                } else {
                    // TODO エラーダイアログ
                }
            }
    }

    companion object {
        fun start(context: Context?) =
            context?.startActivity(
                Intent(context, ProfileSettingActivity::class.java)
            )
    }
}
