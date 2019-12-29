package com.komugirice.icchat

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.qiitaapplication.extension.getDateToString
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.store.UserStore
import com.komugirice.icchat.databinding.ActivityCreateUserBinding
import com.komugirice.icchat.extension.afterTextChanged
import com.komugirice.icchat.ui.createUser.CreateUserViewModel
import com.komugirice.icchat.util.FireStoreUtil
import kotlinx.android.synthetic.main.activity_chat.backImageView
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.container
import timber.log.Timber
import java.util.*

class CreateUserActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateUserBinding
    private lateinit var viewModel: CreateUserViewModel
    private var errorMsg = MutableLiveData<String>()

    var tmpYear: Int = 2000
    var tmpMonth: Int = 1
    var tmpDayOfMonth: Int = 1

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

                if(formState.birthDayError != null) {
                    birthDayEditText.error = getString(formState.birthDayError)
                }

                if(formState.passwordError != null) {
                    passwordEditText.error = getString(formState.passwordError)
                }

                if(formState.passwordConfirmError != null) {
                    passwordConfirmEditText.error = getString(formState.passwordConfirmError)
                }
            })
        }

        errorMsg.observe(this, Observer{
            showDialog("エラー", it)
        })

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
        // ユーザ名の文字数の初期値設定
        userNameLength.text = "${userNameEditText.length()}/20"

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

        calendarImageView.setOnClickListener {
            showDateDialog() {
                birthDayEditText.setText(it.getDateToString())
                // birthDayEditText.afterTextChangedに検知されないので無理やり変える
                birthDayEditText.error = null
            }
        }

        saveButton.setOnClickListener {
            createUser()
        }
    }

    private fun initTextChange() {
        userNameEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                birthDayEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        emailEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                birthDayEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        birthDayEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                birthDayEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        passwordEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                birthDayEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        passwordConfirmEditText.afterTextChanged {
            viewModel.createUserDataChanged(
                userNameEditText.text.toString(),
                emailEditText.text.toString(),
                birthDayEditText.text.toString(),
                passwordEditText.text.toString(),
                passwordConfirmEditText.text.toString()
            )
        }

        // ユーザ名の文字数表示
        userNameEditText.afterTextChanged {
            userNameLength.text = "${userNameEditText.length()}/20"
        }
    }

    private fun showDateDialog(onComplete: (Date) -> Unit) {
        val dialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                tmpYear = year
                tmpMonth = month
                tmpDayOfMonth = dayOfMonth

                val birthDay = Calendar.getInstance().run {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    time
                }
                onComplete.invoke(birthDay)
            }
        }, tmpYear, tmpMonth, tmpDayOfMonth)
        dialog.show()
    }

    private fun createUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(it.isSuccessful) {
                    createUserFireStore()
                } else {

                    when(it.exception) {
                        is FirebaseAuthUserCollisionException ->  {
                            Timber.e(it.exception)
                            errorMsg.postValue(getString(R.string.invalid_duplicate_email))
                        }
                        else -> {
                            Timber.e(it.exception)
                            errorMsg.postValue(getString(R.string.invalid_unknown_exception))
                        }
                    }
                }
            }

    }

    private fun createUserFireStore() {
        val userName = userNameEditText.text.toString().trim()
        val birthDay = birthDayEditText.text.toString().toDate()

        val user = User().apply {
            this.name = userName
            this.birthDay = birthDay
            this.userId = FireStoreUtil.createLoginUserId()
            this.uids.add(FirebaseAuth.getInstance().currentUser?.uid.toString())
        }

        UserStore.registerUser(user){
            // ユーザ登録完了画面に遷移
                CreateUserCompleteActivity.start(this)
        }
    }

    private fun showDialog(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .show();
    }


    companion object {
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, CreateUserActivity::class.java)
            )
    }
}
