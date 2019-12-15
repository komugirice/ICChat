package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.komugirice.icchat.data.firestore.manager.UserManager
import kotlinx.android.synthetic.main.activity_profile_setting.*
import kotlinx.android.synthetic.main.activity_user_name.*
import kotlinx.android.synthetic.main.activity_user_name.backImageView

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
        val userName = userNameEditText.text.toString()
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
        fun start(context: Context?) =
            context?.startActivity(
                Intent(context, UserNameActivity::class.java)
            )
    }
}
