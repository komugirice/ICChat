package com.komugirice.icchat

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.databinding.ActivitySendPasswordBinding
import com.komugirice.icchat.extension.getDateToString
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.firebase.firestore.store.UserStore
import kotlinx.android.synthetic.main.activity_header.view.*
import java.sql.Timestamp
import java.util.*

class SendPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivitySendPasswordBinding
    var inputBirthDay: Date? = null
    var isValid = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_password)
        initialize()
    }

    private fun initialize() {
        initBinding()
        initLayout()
        initClick()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_send_password
        )
        binding.lifecycleOwner = this
    }

    private fun initLayout() {
        // タイトル
        binding.header.titleTextView.text = getString(R.string.send_password_activity_title)
        // 誕生日
        binding.birthDay.text = getString(R.string.no_setting)
    }

    private fun initClick() {
        // 戻る
        binding.header.backImageView.setOnClickListener {
            finish()
        }
        // 誕生日
        binding.birthDay.setOnClickListener {
            showDateDialog()
        }
        // クリアボタン
        binding.clearButton.setOnClickListener {
            binding.mailEditText.text = null
            inputBirthDay = null
            binding.birthDay.text = getString(R.string.no_setting)
        }
        binding.sendButton.setOnClickListener {
            // エラーチェック
            validateSend()
            isValid.observe(this, Observer{
                // メール送信
                sendPasswordMail()
                finish()
            })
        }

        binding.root.setOnClickListener {
            hideKeybord(it)
        }
        binding.contents.setOnClickListener {
            hideKeybord(it)
        }
    }

    private fun validateSend() {
        val inputMail = binding.mailEditText.text.toString()
//        var timestamp: Timestamp? = null
//        inputBirthDay?.apply{
//            timestamp = Timestamp(inputBirthDay?.time ?: 0)
//        }
        FirebaseFirestore.getInstance()
            .collection("${UserStore.USERS}")
            .whereEqualTo(User::email.name, inputMail)
            //.whereEqualTo(User::birthDay.name, timestamp)
            .limit(1L)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result?.toObjects(User::class.java)?.firstOrNull().also {
                        it?.also {
                            val targetDate = it.birthDay
                            // 誕生日一致チェック
                            if(
                                (targetDate == null && inputBirthDay == null) ||
                                targetDate?.getDateToString() == inputBirthDay?.getDateToString()
                            ) {
                                isValid.value = true
                                return@addOnCompleteListener
                            }
                        }
                    }
                }
                // エラーメッセージ
                Toast.makeText(
                    this,
                    R.string.failed_send_password_mail,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun sendPasswordMail() {
        val inputMail = binding.mailEditText.text.toString()
        // メール送信
        FirebaseAuth.getInstance().sendPasswordResetEmail(inputMail)
        // 送信完了メッセージ
        Toast.makeText(
            this,
            R.string.succsess_send_password_mail,
            Toast.LENGTH_LONG).show()
    }

    /**
     * 日付ダイアログ
     *
     */
    private fun showDateDialog() {
        val dialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                // 誕生日更新
                inputBirthDay = Calendar.getInstance().run {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    time
                }
                binding.birthDay.text = inputBirthDay?.getDateToString()
            }
        }, 2000, 0, 1)
        dialog.show()
    }

    companion object {
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, SendPasswordActivity::class.java)
            )
    }
}
