package com.komugirice.icchat.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.komugirice.icchat.data.LoginRepository
import com.komugirice.icchat.data.Result

import com.komugirice.icchat.R
import com.komugirice.icchat.data.model.LoggedInUser

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(userId: String, password: String) {
        // can be launched in a separate asynchronous job
        loginRepository.login(userId, password, _loginResult)
    }

    fun loginDataChanged(userId: String, password: String) {
        if (!isuserIdEmptyValid(userId)) {
            _loginForm.value = LoginFormState(userIdError = R.string.invalid_userId)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder userId validation check
    private fun isuserIdEmptyValid(userId: String): Boolean {
        return userId.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
