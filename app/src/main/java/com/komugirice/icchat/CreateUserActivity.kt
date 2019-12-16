package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityCreateUserBinding
import com.komugirice.icchat.extension.afterTextChanged
import com.komugirice.icchat.ui.createUser.CreateUserViewModel
import kotlinx.android.synthetic.main.activity_chat.backImageView
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.container
import kotlinx.android.synthetic.main.activity_profile_setting.*

class CreateUserActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateUserBinding
    private lateinit var viewModel: CreateUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(CreateUserViewModel::class.java).apply {
            this.createUserFormState.observe(this@CreateUserActivity, Observer {
                val formState = it ?: return@Observer

                saveButton.isEnabled = formState.isDataValid

                if(formState.userNameError != null) {
                    userNameEditText.error = getString(formState.userNameError)
                }

                if(formState.emailError != null) {
                    emailEditText.error = getString(formState.emailError)
                }

                if(formState.passwordError != null) {
                    passwordEditText.error = getString(formState.passwordError)
                }

                if(formState.passwordConfirmError != null) {
                    passwordConfirmEditText.error = getString(formState.passwordConfirmError)
                }
            })
        }
        setContentView(R.layout.activity_create_user)
        initBinding()
        initLayout()
    }

    /**
     * MVVMのBinding
     *
     */
    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_create_user
        )
        binding.lifecycleOwner = this
    }

    /**
     * initLayoutメソッド
     *
     */
    private fun initLayout() {
        initClick()
        initTextChange()
    }

    /**
     * initClickメソッド
     *
     */
    private fun initClick() {
        // <ボタン
        backImageView.setOnClickListener {
            this.onBackPressed()
        }
        container.setOnClickListener {
            hideKeybord(it)
        }
    }

    private fun initTextChange() {
        userNameEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        emailEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        passwordEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        passwordConfirmEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }
    }

    companion object {
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, CreateUserActivity::class.java)
            )
    }
}
