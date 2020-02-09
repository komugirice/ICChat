package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.databinding.ActivityChangePasswordBinding
import com.komugirice.icchat.ui.changePassword.ChangePasswordViewModel
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_header.view.*

class ChangePasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var viewModel: ChangePasswordViewModel
    private var errorMsg = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        initialize()
    }

    private fun initialize() {
        initBinding()
        initViewModel()
        initLayout()
        initClick()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_change_password
        )
        binding.lifecycleOwner = this
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java).apply {
            this.changePasswordFormState.observe(this@ChangePasswordActivity, Observer {
                val formState = it ?: return@Observer

                saveButton.isEnabled = formState.isDataValid

                if(formState.newPasswordError != null) {
                    newPasswordEditText.error = formState.newPasswordError
                }

                if(formState.newPasswordConfirmError != null) {
                    newPasswordConfirmEditText.error = formState.newPasswordConfirmError
                }
            })
            canSubmit.observe(this@ChangePasswordActivity, Observer {
                binding.canSubmit = it
            })
            binding.newPassword = this.newPassword
            binding.newPasswordConfirm = this.newPasswordConfirm

        }
    }
    private fun initLayout() {
        // タイトル
        binding.header.titleTextView.text = getString(R.string.change_password_activity_title)
    }

    private fun initClick() {
        // 戻る
        binding.header.backImageView.setOnClickListener {
            finish()
        }

        // 戻るボタン
        binding.backButton.setOnClickListener {
            finish()
        }

        // 登録ボタン
        binding.saveButton.setOnClickListener{
            changePassword()
        }

        binding.root.setOnClickListener {
            hideKeybord(it)
        }
        binding.contents.setOnClickListener {
            hideKeybord(it)
        }
    }

    private fun changePassword() {
        val password = binding.newPassword?.value
        password?.apply {
            FirebaseAuth.getInstance().currentUser?.updatePassword(this)
            // パスワードを変更しました
            Toast.makeText(
                this@ChangePasswordActivity,
                R.string.success_change_password,
                Toast.LENGTH_LONG).show()
            finish()
        } ?: run {
            // パスワードの変更に失敗しました
            Toast.makeText(
                this,
                R.string.failed_change_password,
                Toast.LENGTH_LONG).show()
        }


    }

    companion object {
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, ChangePasswordActivity::class.java)
            )
    }
}
