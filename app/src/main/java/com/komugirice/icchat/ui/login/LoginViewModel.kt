package com.komugirice.icchat.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.komugirice.icchat.data.LoginRepository
import com.komugirice.icchat.data.Result

import com.komugirice.icchat.R
import com.komugirice.icchat.data.model.LoggedInUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        // can be launched in a separate asynchronous job
        loginRepository.login(email, password, _loginResult)
    }

    fun loginSuccess(email: String?, userName: String?) {
        _loginResult.value = LoginResult(success =
            LoggedInUserView(email = email ?: "", displayName = userName ?: "")
        )
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isemailEmptyValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder email validation check
    private fun isemailEmptyValid(email: String): Boolean {
        return email.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
