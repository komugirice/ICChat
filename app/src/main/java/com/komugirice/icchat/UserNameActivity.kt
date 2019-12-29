package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.extension.afterTextChanged
import kotlinx.android.synthetic.main.activity_user_name.backImageView
import kotlinx.android.synthetic.main.activity_user_name.saveButton
import kotlinx.android.synthetic.main.activity_user_name.userNameEditText
import kotlinx.android.synthetic.main.activity_user_name.userNameLength

class UserNameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_name)
        initialize()
    }

    private fun initialize() {
        initLayout()
        initClick()
    }

    private fun initLayout() {
        userNameEditText.setText(UserManager.myUser.name)

        // ユーザ名の文字数表示
        userNameLength.text = "${userNameEditText.length()}/20"
        userNameEditText.afterTextChanged {
            userNameLength.text = "${userNameEditText.length()}/20"
        }

    }
    private fun initClick() {
        backImageView.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            if(userNameEditText.text.isNotEmpty())
                update()
        }
    }

    private fun update() {
        val userName = userNameEditText.text.toString().trim()
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(UserManager.myUser.userId)
            .update("name", userName)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    UserManager.myUser.name = userName
                    finish()
                }
            }
    }

    companion object {
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, UserNameActivity::class.java)
            )
    }
}
